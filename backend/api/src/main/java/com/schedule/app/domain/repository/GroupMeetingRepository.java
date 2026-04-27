package com.schedule.app.domain.repository;

import java.util.List;

import com.schedule.app.infrastructure.persistence.entity.GroupMeeting;

public interface GroupMeetingRepository {
    GroupMeeting save(GroupMeeting groupMeeting);

    List<GroupMeeting> findByName(String name);

    List<GroupMeeting> findAll();
}
