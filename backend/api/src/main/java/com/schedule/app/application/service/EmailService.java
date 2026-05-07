package com.schedule.app.application.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.schedule.app.infrastructure.persistence.entity.PersonalAppointment;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendReminderEmail(String toEmail, PersonalAppointment appointment) {
        ZonedDateTime startTime = appointment.getTimeSlot().getStartTime()
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
        String formattedTime = startTime.format(formatter);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reminder: " + appointment.getName());
        message.setText("Hello,\n\nThis is a reminder for your upcoming appointment: " + appointment.getName() +
                "\nLocation: " + appointment.getLocation() +
                "\nStart Time: " + formattedTime +
                "\n\nBest regards,\nMake-a-schedule App");

        mailSender.send(message);
    }
}
