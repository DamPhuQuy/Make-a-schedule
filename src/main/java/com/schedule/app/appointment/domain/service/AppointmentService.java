package com.schedule.app.appointment.domain.service;

import com.schedule.app.appointment.domain.model.Appointment;
import com.schedule.app.appointment.domain.port.in.*;
import com.schedule.app.appointment.domain.port.out.AppointmentRepository;
import com.schedule.app.reminder.domain.port.out.ReminderRepository;
import com.schedule.app.user.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService implements CreateAppointmentUseCase, GetAppointmentUseCase,
        UpdateAppointmentUseCase, DeleteAppointmentUseCase, AddUserToAppointmentUseCase,
        AddReminderToAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                            UserRepository userRepository,
                            ReminderRepository reminderRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.reminderRepository = reminderRepository;
    }

    @Override
    public Appointment createAppointment(CreateAppointmentCommand command) {
        if (!userRepository.existsById(command.getUserId())) {
            throw new RuntimeException("User not found with id: " + command.getUserId());
        }

        if (command.getStartHour() >= command.getEndHour()) {
            throw new RuntimeException("Start hour must be less than end hour");
        }

        Appointment appointment = new Appointment();
        appointment.setName(command.getName());
        appointment.setLocation(command.getLocation());
        appointment.setMeetingDate(command.getMeetingDate());
        appointment.setStartHour(command.getStartHour());
        appointment.setEndHour(command.getEndHour());
        appointment.setTypeAppointment(command.getTypeAppointment());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());

        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return appointmentRepository.findByUserId(userId);
    }

    @Override
    public Appointment updateAppointment(Long id, UpdateAppointmentCommand command) {
        Appointment appointment = getAppointmentById(id);

        if (command.getName() != null) {
            appointment.setName(command.getName());
        }
        if (command.getLocation() != null) {
            appointment.setLocation(command.getLocation());
        }
        if (command.getMeetingDate() != null) {
            appointment.setMeetingDate(command.getMeetingDate());
        }
        if (command.getStartHour() != null) {
            appointment.setStartHour(command.getStartHour());
        }
        if (command.getEndHour() != null) {
            appointment.setEndHour(command.getEndHour());
        }
        if (command.getTypeAppointment() != null) {
            appointment.setTypeAppointment(command.getTypeAppointment());
        }

        if (appointment.getStartHour() >= appointment.getEndHour()) {
            throw new RuntimeException("Start hour must be less than end hour");
        }

        appointment.setUpdatedAt(LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    @Override
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new RuntimeException("Appointment not found with id: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    @Override
    public void addUserToAppointment(Long userId, Long appointmentId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new RuntimeException("Appointment not found with id: " + appointmentId);
        }
    }

    @Override
    public void addReminderToAppointment(Long appointmentId, Long reminderId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new RuntimeException("Appointment not found with id: " + appointmentId);
        }
        if (!reminderRepository.existsById(reminderId)) {
            throw new RuntimeException("Reminder not found with id: " + reminderId);
        }
    }
}
