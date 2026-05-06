package com.schedule.app.application.service;

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

    public void sendReminderEmail(String toEmail, PersonalAppointment appointment) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reminder: " + appointment.getName());
        message.setText("Hello,\n\nThis is a reminder for your upcoming appointment: " + appointment.getName() +
                "\nLocation: " + appointment.getLocation() +
                "\nStart Time: " + appointment.getTimeSlot().getStartTime() +
                "\n\nBest regards,\nMake-a-schedule App");

        mailSender.send(message);
    }
}
