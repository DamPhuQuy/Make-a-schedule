package com.schedule.app.infrastructure.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.schedule.app.infrastructure.persistence.entity.GroupMeeting;

public interface GroupMeetingJpaRepository extends JpaRepository<GroupMeeting, Long> {
    List<GroupMeeting> findByName(String name);
}
