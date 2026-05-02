package com.schedule.app.application.service;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schedule.app.domain.repository.AppointmentRepository;
import com.schedule.app.domain.repository.ReminderRepository;
import com.schedule.app.infrastructure.persistence.entity.Appointment;
import com.schedule.app.infrastructure.persistence.entity.Reminder;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private final ReminderRepository reminderRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    public ReminderService(ReminderRepository reminderRepository,
                           AppointmentRepository appointmentRepository,
                           EmailService emailService) {
        this.reminderRepository = reminderRepository;
        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scanAndSendReminders() {
        Instant now = Instant.now();
        List<Reminder> dueReminders = reminderRepository.findAllDueWithDynamicTime(now);

        log.info("[ReminderService] Found {} reminders to process", dueReminders.size());

        for (Reminder reminder : dueReminders) {
            appointmentRepository.findById(reminder.getAppointmentId()).ifPresent(appointment -> {
                try {
                    emailService.sendReminderEmail(appointment.getOwner().getEmail(), appointment);
                    log.info("[ReminderService] Sent reminder for appointment '{}'", appointment.getName());
                    reminderRepository.delete(reminder);
                } catch (Exception e) {
                    log.error("[ReminderService] Failed to send email for appointment id={}: {}",
                            reminder.getAppointmentId(), e.getMessage());
                }
            });
        }
    }
}