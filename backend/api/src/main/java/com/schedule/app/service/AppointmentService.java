package com.schedule.app.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.schedule.app.dto.AppointmentDto;
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

    public List<AppointmentDto> getAllAppointments() {
        User currentUser = getCurrentUser();

        List<AppointmentDto> dtos = appointmentRepository.findByOwnerId(currentUser.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        List<AppointmentDto> groupDtos = groupMeetingRepository.findAll().stream()
                .filter(gm -> gm.getParticipants().stream().anyMatch(u -> u.getId().equals(currentUser.getId())))
                .map(this::mapGroupToDto)
                .collect(Collectors.toList());

        dtos.addAll(groupDtos);
        return dtos;
    }

    @Transactional
    public AppointmentDto createAppointment(AppointmentDto dto, boolean forceReplace, boolean forceJoin) {
        User currentUser = getCurrentUser();
        validateAppointment(dto);

        TimeSlot requestedSlot = new TimeSlot(dto.getStartTime(), dto.getEndTime());

        if (!forceJoin && !forceReplace) {
            List<GroupMeeting> sameNameList = groupMeetingRepository.findByName(dto.getName());
            long newDuration = Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes();

            for (GroupMeeting existing : sameNameList) {
                long existingDuration = Duration.between(existing.getTimeSlot().getStart_time(), existing.getTimeSlot().getEnd_time()).toMinutes();
                if (existingDuration == newDuration) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "GROUP_MEETING:" + existing.getId());
                }
            }
        }

        if (forceJoin) {
            GroupMeeting gm = null;
            if (dto.getId() != null) {
                gm = groupMeetingRepository.findById(dto.getId()).orElse(null);
            } else {
                List<GroupMeeting> sameNameList = groupMeetingRepository.findByName(dto.getName());
                long newDuration = Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes();
                for (GroupMeeting existing : sameNameList) {
                    long existingDuration = Duration.between(existing.getTimeSlot().getStart_time(), existing.getTimeSlot().getEnd_time()).toMinutes();
                    if (existingDuration == newDuration) {
                        gm = existing;
                        break;
                    }
                }
            }

            if (gm == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group meeting not found");
            }

            if (gm.getParticipants().stream().noneMatch(u -> u.getId().equals(currentUser.getId()))) {
                gm.getParticipants().add(currentUser);
                groupMeetingRepository.save(gm);
            }
            return mapGroupToDto(gm);
        }

        if (!forceReplace) {
            List<Appointment> overlaps = appointmentRepository.findByOwnerId(currentUser.getId()).stream()
                .filter(a -> a.getTimeSlot().overlaps(requestedSlot))
                .toList();

            if (!overlaps.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "OVERLAP:" + overlaps.get(0).getId());
            }
        } else {
            List<Appointment> overlaps = appointmentRepository.findByOwnerId(currentUser.getId()).stream()
                .filter(a -> a.getTimeSlot().overlaps(requestedSlot))
                .toList();
            appointmentRepository.deleteAll(overlaps);
        }

        Appointment appointment = Appointment.builder()
                .name(dto.getName())
                .location(dto.getLocation())
                .timeSlot(requestedSlot)
                .owner(currentUser)
                .build();

        appointment = appointmentRepository.save(appointment);

        if (dto.getReminderMinutes() != null) {
            Reminder reminder = Reminder.builder()
                    .appointmentId(appointment.getId())
                    .minutesBefore(dto.getReminderMinutes())
                    .build();
            reminderRepository.save(reminder);
        }

        return mapToDto(appointment);
    }

    // Allows us to quickly seed or create a group meeting if we want to
    @Transactional
    public AppointmentDto createGroupMeeting(AppointmentDto dto) {
        User currentUser = getCurrentUser();
        validateAppointment(dto);
        TimeSlot requestedSlot = new TimeSlot(dto.getStartTime(), dto.getEndTime());

        List<User> participants = new ArrayList<>();
        participants.add(currentUser);

        GroupMeeting gm = GroupMeeting.builder()
            .name(dto.getName())
            .timeSlot(requestedSlot)
            .participants(participants)
            .build();

        groupMeetingRepository.save(gm);
        return mapGroupToDto(gm);
    }

    private void validateAppointment(AppointmentDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment name cannot be empty");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start and end times are required");
        }
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time (duration must be positive)");
        }
    }

    private User getCurrentUser() {
        UserDetailsImpl principal = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private AppointmentDto mapToDto(Appointment param) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(param.getId());
        dto.setName(param.getName());
        dto.setLocation(param.getLocation());
        dto.setStartTime(param.getTimeSlot().getStart_time());
        dto.setEndTime(param.getTimeSlot().getEnd_time());
        dto.setGroupMeeting(false);

        List<Reminder> reminders = reminderRepository.findByAppointmentId(param.getId());
        if (!reminders.isEmpty()) {
            dto.setReminderMinutes(reminders.get(0).getMinutesBefore());
        }
        return dto;
    }

    private AppointmentDto mapGroupToDto(GroupMeeting param) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(param.getId());
        dto.setName(param.getName());
        dto.setLocation("Multiple");
        dto.setStartTime(param.getTimeSlot().getStart_time());
        dto.setEndTime(param.getTimeSlot().getEnd_time());
        dto.setGroupMeeting(true);
        dto.setReminderMinutes(null);
        return dto;
    }
}
