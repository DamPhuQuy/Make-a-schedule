package com.schedule.app.domain.repository;

import java.util.List;
import java.util.Optional;

import com.schedule.app.infrastructure.persistence.entity.GroupMeeting;

public interface GroupMeetingRepository {
    GroupMeeting save(GroupMeeting groupMeeting);

    Optional<GroupMeeting> findById(Long id);

    List<GroupMeeting> findByName(String name);

    List<GroupMeeting> findAll();

    void delete(GroupMeeting groupMeeting);
}
