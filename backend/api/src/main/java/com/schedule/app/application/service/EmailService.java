package com.schedule.app.application.service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.schedule.app.infrastructure.persistence.entity.Appointment;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private static final DateTimeFormatter VIETNAM_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReminderEmail(String toEmail, Appointment appointment) {
        SimpleMailMessage message = new SimpleMailMessage();
        String formattedTime = appointment.getTimeSlot().getStartTime()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(VIETNAM_FORMATTER);
        message.setTo(toEmail);
        message.setSubject("Reminder: " + appointment.getName());
        message.setText("Hello,\n\nThis is a reminder for your upcoming appointment: " + appointment.getName() +
                "\nLocation: " + appointment.getLocation() +
                "\nStart Time: " + formattedTime +
                "\n\nBest regards,\nMake-a-schedule App");
        
        mailSender.send(message);
    }
}
