package com.schedule.app.reminder.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReminderJpaRepository extends JpaRepository<ReminderEntity, Long> {
    Optional<ReminderEntity> findByTitle(String title);
}
