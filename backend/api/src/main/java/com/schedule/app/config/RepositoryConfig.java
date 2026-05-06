package com.schedule.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.schedule.app.adapter.persistence.GroupMeetingRepositoryAdapter;
import com.schedule.app.adapter.persistence.PersonalAppointmentRepositoryAdapter;
import com.schedule.app.adapter.persistence.RefreshTokenRepositoryAdapter;
import com.schedule.app.adapter.persistence.ReminderRepositoryAdapter;
import com.schedule.app.adapter.persistence.UserRepositoryAdapter;
import com.schedule.app.domain.repository.GroupMeetingRepository;
import com.schedule.app.domain.repository.PersonalAppointmentRepository;
import com.schedule.app.domain.repository.RefreshTokenRepository;
import com.schedule.app.domain.repository.ReminderRepository;
import com.schedule.app.domain.repository.UserRepository;
import com.schedule.app.infrastructure.persistence.jpa.GroupMeetingJpaRepository;
import com.schedule.app.infrastructure.persistence.jpa.PersonalAppointmentJpaRepository;
import com.schedule.app.infrastructure.persistence.jpa.RefreshTokenJpaRepository;
import com.schedule.app.infrastructure.persistence.jpa.ReminderJpaRepository;
import com.schedule.app.infrastructure.persistence.jpa.UserJpaRepository;

@Configuration
public class RepositoryConfig {
    @Bean
    public UserRepository userRepository(UserJpaRepository userJpaRepository) {
        return new UserRepositoryAdapter(userJpaRepository);
    }

    @Bean
    public RefreshTokenRepository refreshTokenRepository(RefreshTokenJpaRepository refreshTokenJpaRepository) {
        return new RefreshTokenRepositoryAdapter(refreshTokenJpaRepository);
    }

    @Bean
    public ReminderRepository reminderRepository(ReminderJpaRepository reminderJpaRepository) {
        return new ReminderRepositoryAdapter(reminderJpaRepository);
    }

    @Bean
    public GroupMeetingRepository groupMeetingRepository(GroupMeetingJpaRepository groupMeetingJpaRepository) {
        return new GroupMeetingRepositoryAdapter(groupMeetingJpaRepository);
    }

    @Bean
    public PersonalAppointmentRepository appointmentRepository(PersonalAppointmentJpaRepository appointmentJpaRepository) {
        return new PersonalAppointmentRepositoryAdapter(appointmentJpaRepository);
    }
}
