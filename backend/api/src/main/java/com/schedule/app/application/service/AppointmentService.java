package com.schedule.app.application.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.schedule.app.application.dto.request.CreateAppointmentRequest;
import com.schedule.app.application.dto.response.AppointmentResponse;
import com.schedule.app.application.usecase.AppointmentUseCase;
import com.schedule.app.domain.repository.AppointmentRepository;
import com.schedule.app.domain.repository.GroupMeetingRepository;
import com.schedule.app.domain.repository.ReminderRepository;
import com.schedule.app.domain.repository.UserRepository;
import com.schedule.app.infrastructure.persistence.entity.Appointment;
import com.schedule.app.infrastructure.persistence.entity.GroupMeeting;
import com.schedule.app.infrastructure.persistence.entity.GroupMeetingParticipant;
import com.schedule.app.infrastructure.persistence.entity.Reminder;
import com.schedule.app.infrastructure.persistence.entity.TimeSlot;
import com.schedule.app.infrastructure.persistence.entity.User;
import com.schedule.app.security.UserDetailsImpl;

public class AppointmentService implements AppointmentUseCase {

    private static final String USER_NOT_FOUND = "User not found";

    private final AppointmentRepository appointmentRepository;
    private final GroupMeetingRepository groupMeetingRepository;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, GroupMeetingRepository groupMeetingRepository,
            ReminderRepository reminderRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.groupMeetingRepository = groupMeetingRepository;
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments(UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        List<AppointmentResponse> dtos = appointmentRepository.findByOwnerId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<AppointmentResponse> groupDtos = groupMeetingRepository.findAll().stream()
                .filter(gm -> gm.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(user.getId())))
                .map(this::mapGroupToResponse)
                .toList();

        dtos.addAll(groupDtos);
        return dtos;
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, boolean forceReplace, boolean forceJoin, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));
        validateAppointment(request);

        TimeSlot requestedSlot = new TimeSlot(request.getStartTime(), request.getEndTime());

        if (!forceJoin && !forceReplace) {
            List<GroupMeeting> sameNameList = groupMeetingRepository.findByName(request.getName());
            long newDuration = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

            for (GroupMeeting existing : sameNameList) {
                long existingDuration = Duration.between(existing.getTimeSlot().getStartTime(), existing.getTimeSlot().getEndTime()).toMinutes();
                if (existingDuration == newDuration) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "GROUP_MEETING:" + existing.getId());
                }
            }
        }

        if (forceJoin) {
            GroupMeeting gm = null;
            List<GroupMeeting> sameNameList = groupMeetingRepository.findByName(request.getName());
            long newDuration = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
            for (GroupMeeting existing : sameNameList) {
                long existingDuration = Duration.between(existing.getTimeSlot().getStartTime(), existing.getTimeSlot().getEndTime()).toMinutes();
                if (existingDuration == newDuration) {
                    gm = existing;
                    break;
                }
            }

            if (gm == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group meeting not found");
            }

            if (gm.getParticipants().stream().noneMatch(p -> p.getUser().getId().equals(user.getId()))) {
                GroupMeetingParticipant participant = new GroupMeetingParticipant();
                participant.setGroupMeeting(gm);
                participant.setUser(user);
                gm.getParticipants().add(participant);
                groupMeetingRepository.save(gm);
            }
            return mapGroupToResponse(gm);
        }

        if (!forceReplace) {
            List<Appointment> overlaps = appointmentRepository.findByOwnerId(user.getId()).stream()
                .filter(a -> a.getTimeSlot().overlaps(requestedSlot))
                .toList();

            if (!overlaps.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "OVERLAP:" + overlaps.get(0).getId());
            }
        } else {
            List<Appointment> overlaps = appointmentRepository.findByOwnerId(user.getId()).stream()
                .filter(a -> a.getTimeSlot().overlaps(requestedSlot))
                .toList();
            appointmentRepository.deleteAll(overlaps);
        }

        Appointment appointment = new Appointment();
        appointment.setName(request.getName());
        appointment.setLocation(request.getLocation());
        appointment.setTimeSlot(requestedSlot);
        appointment.setOwner(user);

        appointment = appointmentRepository.save(appointment);

        if (request.getReminderMinutes() != null) {
            Reminder reminder = Reminder.builder()
                    .appointmentId(appointment.getId())
                    .minutesBefore(request.getReminderMinutes())
                    .build();
            reminderRepository.save(reminder);
        }

        return mapToResponse(appointment);
    }

    // Allows us to quickly seed or create a group meeting if we want to
    @Override
    @Transactional
    public AppointmentResponse createGroupMeeting(CreateAppointmentRequest request, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));
        validateAppointment(request);
        TimeSlot requestedSlot = new TimeSlot(request.getStartTime(), request.getEndTime());

        GroupMeeting gm = new GroupMeeting();
        gm.setName(request.getName());
        gm.setTimeSlot(requestedSlot);
        gm.setParticipants(new ArrayList<>());

        GroupMeetingParticipant creatorParticipant = new GroupMeetingParticipant();
        creatorParticipant.setGroupMeeting(gm);
        creatorParticipant.setUser(user);
        gm.getParticipants().add(creatorParticipant);

        if (request.getParticipantUsernames() != null && !request.getParticipantUsernames().isEmpty()) {
            for (String email : request.getParticipantUsernames()) {
                String trimmedEmail = email.trim();
                if (!trimmedEmail.isEmpty() && !trimmedEmail.equals(user.getEmail())) {
                    userRepository.findByEmail(trimmedEmail).ifPresent(participantUser -> {
                        GroupMeetingParticipant participant = new GroupMeetingParticipant();
                        participant.setGroupMeeting(gm);
                        participant.setUser(participantUser);
                        gm.getParticipants().add(participant);
                    });
                }
            }
        }

        groupMeetingRepository.save(gm);
        return mapGroupToResponse(gm);
    }

    private void validateAppointment(CreateAppointmentRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment name cannot be empty");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end times are required");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time (duration must be positive)");
        }
    }

    private AppointmentResponse mapToResponse(Appointment param) {
        List<Reminder> reminders = reminderRepository.findByAppointmentId(param.getId());
        Integer reminderMinutes = reminders.isEmpty() ? null : reminders.get(0).getMinutesBefore();

        return AppointmentResponse.builder()
            .id(param.getId())
            .name(param.getName())
            .location(param.getLocation())
            .startTime(param.getTimeSlot().getStartTime())
            .endTime(param.getTimeSlot().getEndTime())
            .reminderMinutes(reminderMinutes)
            .isGroupMeeting(false)
            .ownerUsername(param.getOwner().getEmail())
            .build();
    }

    private AppointmentResponse mapGroupToResponse(GroupMeeting param) {
        return AppointmentResponse.builder()
            .id(param.getId())
            .name(param.getName())
            .location("Multiple")
            .startTime(param.getTimeSlot().getStartTime())
            .endTime(param.getTimeSlot().getEndTime())
            .reminderMinutes(null)
            .isGroupMeeting(true)
            .ownerUsername("Group")
            .build();
    }
}
