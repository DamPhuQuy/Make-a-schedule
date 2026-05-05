package com.schedule.app.reminder.adapter.out.persistence;

import com.schedule.app.reminder.domain.model.Reminder;
import com.schedule.app.reminder.domain.port.out.ReminderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ReminderRepositoryAdapter implements ReminderRepository {

    private final ReminderJpaRepository jpaRepository;
    private final ReminderMapper mapper;

    public ReminderRepositoryAdapter(ReminderJpaRepository jpaRepository, ReminderMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Reminder save(Reminder reminder) {
        ReminderEntity entity = mapper.toEntity(reminder);
        ReminderEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Reminder> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Reminder> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Optional<Reminder> findByTitle(String title) {
        return jpaRepository.findByTitle(title)
                .map(mapper::toDomain);
    }
}
