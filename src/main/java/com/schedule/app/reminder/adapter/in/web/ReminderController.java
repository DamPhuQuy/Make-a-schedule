package com.schedule.app.reminder.adapter.in.web;

import com.schedule.app.reminder.domain.model.Reminder;
import com.schedule.app.reminder.domain.port.out.ReminderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(origins = "*")
public class ReminderController {

    private final ReminderRepository reminderRepository;

    public ReminderController(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getAllReminders() {
        List<Reminder> reminders = reminderRepository.findAll();
        List<ReminderResponse> responses = reminders.stream()
                .map(ReminderResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> getReminderById(@PathVariable Long id) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reminder not found with id: " + id));
        return ResponseEntity.ok(ReminderResponse.fromDomain(reminder));
    }
}
