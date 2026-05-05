package com.schedule.app.appointment.adapter.in.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schedule.app.appointment.domain.model.Appointment;
import com.schedule.app.appointment.domain.port.in.AddReminderToAppointmentUseCase;
import com.schedule.app.appointment.domain.port.in.AddUserToAppointmentUseCase;
import com.schedule.app.appointment.domain.port.in.CreateAppointmentCommand;
import com.schedule.app.appointment.domain.port.in.CreateAppointmentUseCase;
import com.schedule.app.appointment.domain.port.in.DeleteAppointmentUseCase;
import com.schedule.app.appointment.domain.port.in.GetAppointmentUseCase;
import com.schedule.app.appointment.domain.port.in.JoinGroupMeetingUseCase;
import com.schedule.app.appointment.domain.port.in.ReplaceAppointmentUseCase;
import com.schedule.app.appointment.domain.port.in.UpdateAppointmentCommand;
import com.schedule.app.appointment.domain.port.in.UpdateAppointmentUseCase;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final GetAppointmentUseCase getAppointmentUseCase;
    private final UpdateAppointmentUseCase updateAppointmentUseCase;
    private final DeleteAppointmentUseCase deleteAppointmentUseCase;
    private final AddUserToAppointmentUseCase addUserToAppointmentUseCase;
    private final AddReminderToAppointmentUseCase addReminderToAppointmentUseCase;
    private final JoinGroupMeetingUseCase joinGroupMeetingUseCase;
    private final ReplaceAppointmentUseCase replaceAppointmentUseCase;

    public AppointmentController(CreateAppointmentUseCase createAppointmentUseCase,
                                GetAppointmentUseCase getAppointmentUseCase,
                                UpdateAppointmentUseCase updateAppointmentUseCase,
                                DeleteAppointmentUseCase deleteAppointmentUseCase,
                                AddUserToAppointmentUseCase addUserToAppointmentUseCase,
                                AddReminderToAppointmentUseCase addReminderToAppointmentUseCase,
                                JoinGroupMeetingUseCase joinGroupMeetingUseCase,
                                ReplaceAppointmentUseCase replaceAppointmentUseCase) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.getAppointmentUseCase = getAppointmentUseCase;
        this.updateAppointmentUseCase = updateAppointmentUseCase;
        this.deleteAppointmentUseCase = deleteAppointmentUseCase;
        this.addUserToAppointmentUseCase = addUserToAppointmentUseCase;
        this.addReminderToAppointmentUseCase = addReminderToAppointmentUseCase;
        this.joinGroupMeetingUseCase = joinGroupMeetingUseCase;
        this.replaceAppointmentUseCase = replaceAppointmentUseCase;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@RequestBody CreateAppointmentRequest request) {
        CreateAppointmentCommand command = new CreateAppointmentCommand(
                request.getUserId(),
                request.getName(),
                request.getLocation(),
                request.getMeetingDate(),
                request.getStartHour(),
                request.getEndHour(),
                request.getTypeAppointment()
        );
        Appointment appointment = createAppointmentUseCase.createAppointment(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.fromDomain(appointment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = getAppointmentUseCase.getAppointmentById(id);
        return ResponseEntity.ok(AppointmentResponse.fromDomain(appointment));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        List<Appointment> appointments = getAppointmentUseCase.getAllAppointments();
        List<AppointmentResponse> responses = appointments.stream()
                .map(AppointmentResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByUserId(@PathVariable Long userId) {
        List<Appointment> appointments = getAppointmentUseCase.getAppointmentsByUserId(userId);
        List<AppointmentResponse> responses = appointments.stream()
                .map(AppointmentResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(@PathVariable Long id,
                                                                 @RequestBody UpdateAppointmentRequest request) {
        UpdateAppointmentCommand command = new UpdateAppointmentCommand(
                request.getName(),
                request.getLocation(),
                request.getMeetingDate(),
                request.getStartHour(),
                request.getEndHour(),
                request.getTypeAppointment()
        );
        Appointment appointment = updateAppointmentUseCase.updateAppointment(id, command);
        return ResponseEntity.ok(AppointmentResponse.fromDomain(appointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        deleteAppointmentUseCase.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{appointmentId}/users/{userId}")
    public ResponseEntity<Void> addUserToAppointment(@PathVariable Long appointmentId,
                                                     @PathVariable Long userId) {
        addUserToAppointmentUseCase.addUserToAppointment(userId, appointmentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{appointmentId}/reminders/{reminderId}")
    public ResponseEntity<Void> addReminderToAppointment(@PathVariable Long appointmentId,
                                                         @PathVariable Long reminderId) {
        addReminderToAppointmentUseCase.addReminderToAppointment(appointmentId, reminderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/group-meetings/{groupMeetingId}/join/{userId}")
    public ResponseEntity<Void> joinGroupMeeting(@PathVariable Long groupMeetingId,
                                                 @PathVariable Long userId) {
        joinGroupMeetingUseCase.joinGroupMeeting(userId, groupMeetingId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{oldAppointmentId}/replace")
    public ResponseEntity<AppointmentResponse> replaceAppointment(@PathVariable Long oldAppointmentId,
                                                                  @RequestBody CreateAppointmentRequest request) {
        CreateAppointmentCommand command = new CreateAppointmentCommand(
                request.getUserId(),
                request.getName(),
                request.getLocation(),
                request.getMeetingDate(),
                request.getStartHour(),
                request.getEndHour(),
                request.getTypeAppointment()
        );
        Appointment appointment = replaceAppointmentUseCase.replaceAppointment(oldAppointmentId, command);
        return ResponseEntity.ok(AppointmentResponse.fromDomain(appointment));
    }
}
