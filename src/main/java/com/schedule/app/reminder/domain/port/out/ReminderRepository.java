package com.schedule.app.reminder.domain.port.out;

import com.schedule.app.reminder.domain.model.Reminder;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository {
    Reminder save(Reminder reminder);
    Optional<Reminder> findById(Long id);
    List<Reminder> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    Optional<Reminder> findByTitle(String title);
}
