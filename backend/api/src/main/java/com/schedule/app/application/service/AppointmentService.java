package com.schedule.app.application.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.schedule.app.domain.repository.PersonalAppointmentRepository;
import com.schedule.app.domain.repository.ReminderRepository;
import com.schedule.app.domain.repository.UserRepository;
import com.schedule.app.infrastructure.persistence.entity.Appointment;
import com.schedule.app.infrastructure.persistence.entity.GroupMeeting;
import com.schedule.app.infrastructure.persistence.entity.GroupMeetingParticipant;
import com.schedule.app.infrastructure.persistence.entity.PersonalAppointment;
import com.schedule.app.infrastructure.persistence.entity.Reminder;
import com.schedule.app.infrastructure.persistence.entity.TimeSlot;
import com.schedule.app.infrastructure.persistence.entity.User;
import com.schedule.app.security.UserDetailsImpl;

public class AppointmentService implements AppointmentUseCase {

    private static final String USER_NOT_FOUND = "User not found";

    private final PersonalAppointmentRepository appointmentRepository;
    private final GroupMeetingRepository groupMeetingRepository;
    private final CreateReminderUseCase createReminderUseCase;
    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final com.schedule.app.domain.repository.AppointmentRepository baseRepository;

    public AppointmentService(PersonalAppointmentRepository appointmentRepository, GroupMeetingRepository groupMeetingRepository,
            CreateReminderUseCase createReminderUseCase, ReminderRepository reminderRepository, UserRepository userRepository,
            AppointmentRepository baseRepository) {
        this.appointmentRepository = appointmentRepository;
        this.groupMeetingRepository = groupMeetingRepository;
        this.createReminderUseCase = createReminderUseCase;
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.baseRepository = baseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentConflictResponse validateAppointment(ValidateAppointmentRequest request, UserDetailsImpl currentUser) {
        User user = getUserOrThrow(currentUser.getId());
        validateAppointmentBasicFields(request.getName(), request.getStartTime(), request.getEndTime());

        var groupMeetingMatch = findMatchingGroupMeeting(request);
        if (groupMeetingMatch.isPresent()) {
            return buildGroupMeetingMatchResponse(groupMeetingMatch.get());
        }

        var overlappingAppointment = findOverlappingAppointment(user.getId(), request.getStartTime(), request.getEndTime());
        if (overlappingAppointment.isPresent()) {
            return buildTimeOverlapResponse(overlappingAppointment.get());
        }

        return buildNoConflictResponse();
    }

    private void validateAppointmentBasicFields(String name, Instant startTime, Instant endTime) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment name cannot be empty");
        }
        if (startTime == null || endTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end times are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
    }

    private Optional<GroupMeeting> findMatchingGroupMeeting(ValidateAppointmentRequest request) {
        return groupMeetingRepository.findByName(request.getName()).stream()
            .filter(gm -> gm.getTimeSlot().getStartTime().equals(request.getStartTime())
                       && gm.getTimeSlot().getEndTime().equals(request.getEndTime()))
            .findFirst();
    }

    private Optional<Appointment> findOverlappingAppointment(
            Long userId, Instant startTime, Instant endTime) {
        return baseRepository.findOverlappingAppointmentsForUser(userId, startTime, endTime).stream()
            .findFirst();
    }

    private AppointmentConflictResponse buildGroupMeetingMatchResponse(GroupMeeting match) {
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

    private AppointmentConflictResponse buildTimeOverlapResponse(
            Appointment conflict) {
        String conflictType = conflict instanceof GroupMeeting ? "group meeting" : "appointment";
        return AppointmentConflictResponse.builder()
            .conflictType(AppointmentConflictResponse.ConflictType.TIME_OVERLAP)
            .message("You already have a " + conflictType + " at this time. Would you like to choose another time or replace it?")
            .details(AppointmentConflictResponse.ConflictDetails.builder()
                .conflictingAppointmentId(conflict.getId())
                .conflictingAppointmentName(conflict.getName())
                .conflictingStartTime(conflict.getTimeSlot().getStartTime())
                .conflictingEndTime(conflict.getTimeSlot().getEndTime())
                .build())
            .build();
    }

    private AppointmentConflictResponse buildNoConflictResponse() {
        return AppointmentConflictResponse.builder()
            .conflictType(AppointmentConflictResponse.ConflictType.NONE)
            .message("No conflicts found. You can proceed with creating the appointment.")
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments(UserDetailsImpl currentUser) {
        User user = getUserOrThrow(currentUser.getId());

        List<AppointmentResponse> allAppointments = new ArrayList<>();
        allAppointments.addAll(getPersonalAppointments(user.getId()));
        allAppointments.addAll(getGroupMeetingsForUser(user.getId()));

        return allAppointments;
    }

    private List<AppointmentResponse> getPersonalAppointments(Long userId) {
        return appointmentRepository.findByOwnerId(userId).stream()
            .map(this::mapToResponse)
            .toList();
    }

    private List<AppointmentResponse> getGroupMeetingsForUser(Long userId) {
        return groupMeetingRepository.findAll().stream()
            .filter(gm -> isUserParticipant(gm, userId))
            .map(this::mapGroupToResponse)
            .toList();
    }

    private boolean isUserParticipant(GroupMeeting gm, Long userId) {
        return gm.getParticipants().stream()
            .anyMatch(p -> p.getUser().getId().equals(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id, UserDetailsImpl currentUser) {
        User user = getUserOrThrow(currentUser.getId());

        var personalAppointment = appointmentRepository.findById(id);
        if (personalAppointment.isPresent()) {
            return handlePersonalAppointmentAccess(personalAppointment.get(), user.getId());
        }

        var groupMeeting = groupMeetingRepository.findById(id);
        if (groupMeeting.isPresent()) {
            return handleGroupMeetingAccess(groupMeeting.get(), user.getId());
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
    }

    private AppointmentResponse handlePersonalAppointmentAccess(PersonalAppointment appointment, Long userId) {
        if (!appointment.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this appointment");
        }
        return mapToResponse(appointment);
    }

    private AppointmentResponse handleGroupMeetingAccess(GroupMeeting gm, Long userId) {
        if (!isUserParticipant(gm, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant of this group meeting");
        }
        return mapGroupToResponse(gm);
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, UserDetailsImpl currentUser) {
        User user = getUserOrThrow(currentUser.getId());
        validateAppointmentBasicFields(request.getName(), request.getStartTime(), request.getEndTime());

        // Debug logging
        System.out.println("DEBUG - forceReplace: " + request.isForceReplace());
        System.out.println("DEBUG - forceJoin: " + request.isForceJoin());

        if (request.isForceJoin()) {
            return handleForceJoin(request, user);
        }

        checkConflictsIfNeeded(request, user.getId());
        handleForceReplaceIfNeeded(request, user.getId());

        PersonalAppointment appointment = createPersonalAppointment(request, user);
        appointment = appointmentRepository.save(appointment);

        createReminderIfNeeded(appointment.getId(), request.getReminderMinutes());

        return mapToResponse(appointment);
    }

    private AppointmentResponse handleForceJoin(CreateAppointmentRequest request, User user) {
        GroupMeeting gm = findMatchingGroupMeetingOrThrow(request);
        addUserToGroupMeetingIfNeeded(gm, user);
        return mapGroupToResponse(gm);
    }

    private GroupMeeting findMatchingGroupMeetingOrThrow(CreateAppointmentRequest request) {
        return groupMeetingRepository.findByName(request.getName()).stream()
            .filter(gm -> gm.getTimeSlot().getStartTime().equals(request.getStartTime())
                       && gm.getTimeSlot().getEndTime().equals(request.getEndTime()))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group meeting not found"));
    }

    private void addUserToGroupMeetingIfNeeded(GroupMeeting gm, User user) {
        boolean isAlreadyParticipant = gm.getParticipants().stream()
            .anyMatch(p -> p.getUser().getId().equals(user.getId()));

        if (!isAlreadyParticipant) {
            GroupMeetingParticipant participant = new GroupMeetingParticipant();
            participant.setGroupMeeting(gm);
            participant.setUser(user);
            gm.getParticipants().add(participant);
            groupMeetingRepository.save(gm);
        }
    }

    private void checkConflictsIfNeeded(CreateAppointmentRequest request, Long userId) {
        if (!request.isForceReplace()) {
            List<Appointment> overlapping =
                baseRepository.findOverlappingAppointmentsForUser(userId, request.getStartTime(), request.getEndTime());

            if (!overlapping.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Time conflict with existing appointment. Please choose another time or use forceReplace.");
            }
        }
    }

    private void handleForceReplaceIfNeeded(CreateAppointmentRequest request, Long userId) {
        if (request.isForceReplace()) {
            List<Appointment> overlapping =
                baseRepository.findOverlappingAppointmentsForUser(userId, request.getStartTime(), request.getEndTime());

            for (Appointment overlap : overlapping) {
                deleteOrRemoveUserFromAppointment(overlap, userId);
            }
        }
    }

    private void deleteOrRemoveUserFromAppointment(Appointment appointment, Long userId) {
        if (appointment instanceof GroupMeeting) {
            removeUserFromGroupMeeting((GroupMeeting) appointment, userId);
        } else if (appointment instanceof PersonalAppointment) {
            appointmentRepository.delete((PersonalAppointment) appointment);
        }
    }

    private void removeUserFromGroupMeeting(GroupMeeting gm, Long userId) {
        gm.getParticipants().removeIf(p -> p.getUser().getId().equals(userId));
        if (gm.getParticipants().isEmpty()) {
            groupMeetingRepository.delete(gm);
        } else {
            groupMeetingRepository.save(gm);
        }
    }

    private PersonalAppointment createPersonalAppointment(CreateAppointmentRequest request, User user) {
        PersonalAppointment appointment = new PersonalAppointment();
        appointment.setName(request.getName());
        appointment.setLocation(request.getLocation());
        appointment.setTimeSlot(new TimeSlot(request.getStartTime(), request.getEndTime()));
        appointment.setOwner(user);
        return appointment;
    }

    private void createReminderIfNeeded(Long appointmentId, Integer reminderMinutes) {
        if (reminderMinutes != null) {
            createReminderUseCase.execute(appointmentId, reminderMinutes);
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public AppointmentResponse createGroupMeeting(CreateAppointmentRequest request, UserDetailsImpl currentUser) {
        User user = getUserOrThrow(currentUser.getId());
        validateAppointmentBasicFields(request.getName(), request.getStartTime(), request.getEndTime());

        checkConflictsIfNeeded(request, user.getId());
        handleForceReplaceIfNeeded(request, user.getId());

        GroupMeeting gm = createGroupMeetingWithCreator(request, user);
        addParticipantsToGroupMeeting(gm, request.getParticipantUsernames(), user.getEmail());

        groupMeetingRepository.save(gm);
        return mapGroupToResponse(gm);
    }

    private GroupMeeting createGroupMeetingWithCreator(CreateAppointmentRequest request, User creator) {
        GroupMeeting gm = new GroupMeeting();
        gm.setName(request.getName());
        gm.setTimeSlot(new TimeSlot(request.getStartTime(), request.getEndTime()));
        gm.setParticipants(new ArrayList<>());

        GroupMeetingParticipant creatorParticipant = new GroupMeetingParticipant();
        creatorParticipant.setGroupMeeting(gm);
        creatorParticipant.setUser(creator);
        gm.getParticipants().add(creatorParticipant);

        return gm;
    }

    private void addParticipantsToGroupMeeting(GroupMeeting gm, List<String> participantEmails, String creatorEmail) {
        if (participantEmails == null || participantEmails.isEmpty()) {
            return;
        }

        for (String email : participantEmails) {
            String trimmedEmail = email.trim();
            if (!trimmedEmail.isEmpty() && !trimmedEmail.equals(creatorEmail)) {
                userRepository.findByEmail(trimmedEmail).ifPresent(participantUser -> {
                    GroupMeetingParticipant participant = new GroupMeetingParticipant();
                    participant.setGroupMeeting(gm);
                    participant.setUser(participantUser);
                    gm.getParticipants().add(participant);
                });
            }
        }
    }

    @Override
    @Transactional
    public void deleteAppointment(Long id, UserDetailsImpl currentUser) {
        User user = getUserOrThrow(currentUser.getId());

        var personalAppointment = appointmentRepository.findById(id);
        if (personalAppointment.isPresent()) {
            deletePersonalAppointment(personalAppointment.get(), user.getId());
            return;
        }

        var groupMeeting = groupMeetingRepository.findById(id);
        if (groupMeeting.isPresent()) {
            removeUserFromGroupMeetingOrDelete(groupMeeting.get(), user.getId());
            return;
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found");
    }

    private void deletePersonalAppointment(PersonalAppointment appointment, Long userId) {
        if (!appointment.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this appointment");
        }
        appointmentRepository.delete(appointment);
    }

    private void removeUserFromGroupMeetingOrDelete(GroupMeeting gm, Long userId) {
        if (!isUserParticipant(gm, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant of this group meeting");
        }

        gm.getParticipants().removeIf(p -> p.getUser().getId().equals(userId));

        if (gm.getParticipants().isEmpty()) {
            groupMeetingRepository.delete(gm);
        } else {
            groupMeetingRepository.save(gm);
        }
    }

    private AppointmentResponse mapToResponse(PersonalAppointment param) {
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
            .toList();

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
