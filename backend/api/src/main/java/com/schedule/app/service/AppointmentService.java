package com.schedule.app.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.schedule.app.dto.request.CreateAppointmentRequest;
import com.schedule.app.dto.response.AppointmentResponse;
import com.schedule.app.entity.Appointment;
import com.schedule.app.entity.GroupMeeting;
import com.schedule.app.entity.Reminder;
import com.schedule.app.entity.TimeSlot;
import com.schedule.app.entity.User;
import com.schedule.app.repository.AppointmentRepository;
import com.schedule.app.repository.GroupMeetingRepository;
import com.schedule.app.repository.ReminderRepository;
import com.schedule.app.repository.UserRepository;
import com.schedule.app.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final GroupMeetingRepository groupMeetingRepository;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public List<AppointmentResponse> getAllAppointments(UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        List<AppointmentResponse> dtos = appointmentRepository.findByOwnerId(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<AppointmentResponse> groupDtos = groupMeetingRepository.findAll().stream()
                .filter(gm -> gm.getParticipants().stream().anyMatch(u -> u.getId().equals(user.getId())))
                .map(this::mapGroupToResponse)
                .toList();

        dtos.addAll(groupDtos);
        return dtos;
    }

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, boolean forceReplace, boolean forceJoin, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        validateAppointment(request);

        TimeSlot requestedSlot = new TimeSlot(request.getStartTime(), request.getEndTime());

        if (!forceJoin && !forceReplace) {
            List<GroupMeeting> sameNameList = groupMeetingRepository.findByName(request.getName());
            long newDuration = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

            for (GroupMeeting existing : sameNameList) {
                long existingDuration = Duration.between(existing.getTimeSlot().getStart_time(), existing.getTimeSlot().getEnd_time()).toMinutes();
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
                long existingDuration = Duration.between(existing.getTimeSlot().getStart_time(), existing.getTimeSlot().getEnd_time()).toMinutes();
                if (existingDuration == newDuration) {
                    gm = existing;
                    break;
                }
            }

            if (gm == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group meeting not found");
            }

            if (gm.getParticipants().stream().noneMatch(u -> u.getId().equals(user.getId()))) {
                gm.getParticipants().add(user);
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

        Appointment appointment = Appointment.builder()
                .name(request.getName())
                .location(request.getLocation())
                .timeSlot(requestedSlot)
                .owner(user)
                .build();

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
    @Transactional
    public AppointmentResponse createGroupMeeting(CreateAppointmentRequest request, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        validateAppointment(request);
        TimeSlot requestedSlot = new TimeSlot(request.getStartTime(), request.getEndTime());

        List<User> participants = new ArrayList<>();
        participants.add(user);

        GroupMeeting gm = GroupMeeting.builder()
            .name(request.getName())
            .timeSlot(requestedSlot)
            .participants(participants)
            .build();

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

    private User getCurrentUser(UserDetailsImpl currentUser) {
        return userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private AppointmentResponse mapToResponse(Appointment param) {
        List<Reminder> reminders = reminderRepository.findByAppointmentId(param.getId());
        Integer reminderMinutes = reminders.isEmpty() ? null : reminders.get(0).getMinutesBefore();

        return AppointmentResponse.builder()
            .id(param.getId())
            .name(param.getName())
            .location(param.getLocation())
            .startTime(param.getTimeSlot().getStart_time())
            .endTime(param.getTimeSlot().getEnd_time())
            .reminderMinutes(reminderMinutes)
            .isGroupMeeting(false)
            .ownerUsername(param.getOwner().getUsername())
            .build();
    }

    private AppointmentResponse mapGroupToResponse(GroupMeeting param) {
        return AppointmentResponse.builder()
            .id(param.getId())
            .name(param.getName())
            .location("Multiple")
            .startTime(param.getTimeSlot().getStart_time())
            .endTime(param.getTimeSlot().getEnd_time())
            .reminderMinutes(null)
            .isGroupMeeting(true)
            .ownerUsername("Group")
            .build();
    }
}
