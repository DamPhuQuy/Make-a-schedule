package com.schedule.app.application.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.schedule.app.application.dto.request.CreateAppointmentRequest;
import com.schedule.app.application.dto.request.ValidateAppointmentRequest;
import com.schedule.app.application.dto.response.AppointmentConflictResponse;
import com.schedule.app.application.dto.response.AppointmentResponse;
import com.schedule.app.application.usecase.AppointmentUseCase;
import com.schedule.app.application.usecase.CreateReminderUseCase;
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
    private final CreateReminderUseCase createReminderUseCase;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, GroupMeetingRepository groupMeetingRepository,
            CreateReminderUseCase createReminderUseCase, ReminderRepository reminderRepository, UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.groupMeetingRepository = groupMeetingRepository;
        this.createReminderUseCase = createReminderUseCase;
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentConflictResponse validateAppointment(ValidateAppointmentRequest request, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment name cannot be empty");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end times are required");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        TimeSlot requestedSlot = new TimeSlot(request.getStartTime(), request.getEndTime());

        // Check for exact match with existing group meetings (same name, start time, and end time)
        List<GroupMeeting> matchingGroupMeetings = groupMeetingRepository.findByName(request.getName()).stream()
            .filter(gm -> gm.getTimeSlot().getStartTime().equals(request.getStartTime())
                       && gm.getTimeSlot().getEndTime().equals(request.getEndTime()))
            .toList();

        if (!matchingGroupMeetings.isEmpty()) {
            GroupMeeting match = matchingGroupMeetings.get(0);
            List<String> participantEmails = match.getParticipants().stream()
                .map(p -> p.getUser().getEmail())
                .toList();

            return AppointmentConflictResponse.builder()
                .conflictType(AppointmentConflictResponse.ConflictType.GROUP_MEETING_MATCH)
                .message("A group meeting with the same name and time already exists. Would you like to join it?")
                .details(AppointmentConflictResponse.ConflictDetails.builder()
                    .matchingGroupMeetingId(match.getId())
                    .matchingGroupMeetingName(match.getName())
                    .matchingGroupStartTime(match.getTimeSlot().getStartTime())
                    .matchingGroupEndTime(match.getTimeSlot().getEndTime())
                    .participants(participantEmails)
                    .build())
                .build();
        }

        // Check for time overlap with user's normal appointments
        List<Appointment> overlappingAppointments = appointmentRepository.findByOwnerId(user.getId()).stream()
            .filter(a -> a.getTimeSlot().overlaps(requestedSlot))
            .toList();

        if (!overlappingAppointments.isEmpty()) {
            Appointment conflict = overlappingAppointments.get(0);
            return AppointmentConflictResponse.builder()
                .conflictType(AppointmentConflictResponse.ConflictType.TIME_OVERLAP)
                .message("You already have an appointment at this time. Would you like to choose another time or replace it?")
                .details(AppointmentConflictResponse.ConflictDetails.builder()
                    .conflictingAppointmentId(conflict.getId())
                    .conflictingAppointmentName(conflict.getName())
                    .conflictingStartTime(conflict.getTimeSlot().getStartTime())
                    .conflictingEndTime(conflict.getTimeSlot().getEndTime())
                    .build())
                .build();
        }

        // Check for time overlap with user's group meetings
        List<GroupMeeting> overlappingGroupMeetings = groupMeetingRepository.findAll().stream()
            .filter(gm -> gm.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId())))
            .filter(gm -> gm.getTimeSlot().overlaps(requestedSlot))
            .toList();

        if (!overlappingGroupMeetings.isEmpty()) {
            GroupMeeting conflict = overlappingGroupMeetings.get(0);
            return AppointmentConflictResponse.builder()
                .conflictType(AppointmentConflictResponse.ConflictType.TIME_OVERLAP)
                .message("You already have a group meeting at this time. Would you like to choose another time or replace it?")
                .details(AppointmentConflictResponse.ConflictDetails.builder()
                    .conflictingAppointmentId(conflict.getId())
                    .conflictingAppointmentName(conflict.getName())
                    .conflictingStartTime(conflict.getTimeSlot().getStartTime())
                    .conflictingEndTime(conflict.getTimeSlot().getEndTime())
                    .build())
                .build();
        }

        return AppointmentConflictResponse.builder()
            .conflictType(AppointmentConflictResponse.ConflictType.NONE)
            .message("No conflicts found. You can proceed with creating the appointment.")
            .build();
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
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        // Try to find as normal appointment
        var appointment = appointmentRepository.findById(id);
        if (appointment.isPresent()) {
            Appointment appt = appointment.get();
            if (!appt.getOwner().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this appointment");
            }
            return mapToResponse(appt);
        }

        // Try to find as group meeting
        var groupMeeting = groupMeetingRepository.findById(id);
        if (groupMeeting.isPresent()) {
            GroupMeeting gm = groupMeeting.get();
            boolean isParticipant = gm.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));
            if (!isParticipant) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant of this group meeting");
            }
            return mapGroupToResponse(gm);
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));
        validateAppointment(request);

        TimeSlot requestedSlot = new TimeSlot(request.getStartTime(), request.getEndTime());

        // Handle forceJoin: add user to existing group meeting
        if (request.isForceJoin()) {
            List<GroupMeeting> matchingGroupMeetings = groupMeetingRepository.findByName(request.getName()).stream()
                .filter(gm -> gm.getTimeSlot().getStartTime().equals(request.getStartTime())
                           && gm.getTimeSlot().getEndTime().equals(request.getEndTime()))
                .toList();

            if (matchingGroupMeetings.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group meeting not found");
            }

            GroupMeeting gm = matchingGroupMeetings.get(0);

            // Check if user is already a participant
            if (gm.getParticipants().stream().noneMatch(p -> p.getUser().getId().equals(user.getId()))) {
                GroupMeetingParticipant participant = new GroupMeetingParticipant();
                participant.setGroupMeeting(gm);
                participant.setUser(user);
                gm.getParticipants().add(participant);
                groupMeetingRepository.save(gm);
            }
            return mapGroupToResponse(gm);
        }

        // Backend validation: Check for conflicts when not forcing replace
        if (!request.isForceReplace()) {
            // Check for overlapping normal appointments
            List<Appointment> overlappingAppointments = appointmentRepository.findByOwnerId(user.getId()).stream()
                .filter(a -> a.getTimeSlot().overlaps(requestedSlot))
                .toList();

            if (!overlappingAppointments.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Time conflict with existing appointment. Please choose another time or use forceReplace.");
            }

            // Check for overlapping group meetings
            List<GroupMeeting> overlappingGroupMeetings = groupMeetingRepository.findAll().stream()
                .filter(gm -> gm.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(user.getId())))
                .filter(gm -> gm.getTimeSlot().overlaps(requestedSlot))
                .toList();

            if (!overlappingGroupMeetings.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Time conflict with existing group meeting. Please choose another time or use forceReplace.");
            }
        }

        // Handle forceReplace: delete overlapping appointments and group meetings
        if (request.isForceReplace()) {
            // Delete overlapping normal appointments
            List<Appointment> overlappingAppointments = appointmentRepository.findByOwnerId(user.getId()).stream()
                .filter(a -> a.getTimeSlot().overlaps(requestedSlot))
                .toList();
            appointmentRepository.deleteAll(overlappingAppointments);

            // Remove user from overlapping group meetings
            List<GroupMeeting> overlappingGroupMeetings = groupMeetingRepository.findAll().stream()
                .filter(gm -> gm.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(user.getId())))
                .filter(gm -> gm.getTimeSlot().overlaps(requestedSlot))
                .toList();

            for (GroupMeeting gm : overlappingGroupMeetings) {
                gm.getParticipants().removeIf(p -> p.getUser().getId().equals(user.getId()));
                if (gm.getParticipants().isEmpty()) {
                    groupMeetingRepository.delete(gm);
                } else {
                    groupMeetingRepository.save(gm);
                }
            }
        }

        // Create new appointment
        Appointment appointment = new Appointment();
        appointment.setName(request.getName());
        appointment.setLocation(request.getLocation());
        appointment.setTimeSlot(requestedSlot);
        appointment.setOwner(user);

        appointment = appointmentRepository.save(appointment);

        if (request.getReminderMinutes() != null) {
            createReminderUseCase.execute(appointment.getId(), request.getReminderMinutes());
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

        // Backend validation: Check for conflicts when not forcing replace
        if (!request.isForceReplace()) {
            // Check for overlapping normal appointments
            List<Appointment> overlappingAppointments = appointmentRepository.findByOwnerId(user.getId()).stream()
                .filter(a -> a.getTimeSlot().overlaps(requestedSlot))
                .toList();

            if (!overlappingAppointments.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Time conflict with existing appointment. Please choose another time or use forceReplace.");
            }

            // Check for overlapping group meetings
            List<GroupMeeting> overlappingGroupMeetings = groupMeetingRepository.findAll().stream()
                .filter(gm -> gm.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(user.getId())))
                .filter(gm -> gm.getTimeSlot().overlaps(requestedSlot))
                .toList();

            if (!overlappingGroupMeetings.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Time conflict with existing group meeting. Please choose another time or use forceReplace.");
            }
        }

        // Handle forceReplace: delete overlapping appointments and remove from group meetings
        if (request.isForceReplace()) {
            // Delete overlapping normal appointments
            List<Appointment> overlappingAppointments = appointmentRepository.findByOwnerId(user.getId()).stream()
                .filter(a -> a.getTimeSlot().overlaps(requestedSlot))
                .toList();
            appointmentRepository.deleteAll(overlappingAppointments);

            // Remove user from overlapping group meetings
            List<GroupMeeting> overlappingGroupMeetings = groupMeetingRepository.findAll().stream()
                .filter(gm -> gm.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(user.getId())))
                .filter(gm -> gm.getTimeSlot().overlaps(requestedSlot))
                .toList();

            for (GroupMeeting gm : overlappingGroupMeetings) {
                gm.getParticipants().removeIf(p -> p.getUser().getId().equals(user.getId()));
                if (gm.getParticipants().isEmpty()) {
                    groupMeetingRepository.delete(gm);
                } else {
                    groupMeetingRepository.save(gm);
                }
            }
        }

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

    @Override
    @Transactional
    public void deleteAppointment(Long id, UserDetailsImpl currentUser) {
        var user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        // Try to find as normal appointment
        var appointment = appointmentRepository.findById(id);
        if (appointment.isPresent()) {
            Appointment appt = appointment.get();
            if (!appt.getOwner().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this appointment");
            }
            appointmentRepository.delete(appt);
            return;
        }

        // Try to find as group meeting
        var groupMeeting = groupMeetingRepository.findById(id);
        if (groupMeeting.isPresent()) {
            GroupMeeting gm = groupMeeting.get();
            boolean isParticipant = gm.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));
            if (!isParticipant) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant of this group meeting");
            }

            // Remove user from group meeting
            gm.getParticipants().removeIf(p -> p.getUser().getId().equals(user.getId()));

            // If no participants left, delete the group meeting
            if (gm.getParticipants().isEmpty()) {
                groupMeetingRepository.delete(gm);
            } else {
                groupMeetingRepository.save(gm);
            }
            return;
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
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
            .appointmentType(com.schedule.app.domain.model.AppointmentType.NORMAL_APPOINTMENT)
            .participants(null)
            .build();
    }

    private AppointmentResponse mapGroupToResponse(GroupMeeting param) {
        List<String> participantEmails = param.getParticipants().stream()
            .map(p -> p.getUser().getEmail())
            .collect(Collectors.toList());

        return AppointmentResponse.builder()
            .id(param.getId())
            .name(param.getName())
            .location("Multiple")
            .startTime(param.getTimeSlot().getStartTime())
            .endTime(param.getTimeSlot().getEndTime())
            .reminderMinutes(null)
            .isGroupMeeting(true)
            .ownerUsername("Group")
            .appointmentType(com.schedule.app.domain.model.AppointmentType.GROUP_MEETING)
            .participants(participantEmails)
            .build();
    }
}
