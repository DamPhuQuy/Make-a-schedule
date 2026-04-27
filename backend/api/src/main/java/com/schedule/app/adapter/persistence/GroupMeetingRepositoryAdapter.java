package com.schedule.app.adapter.persistence;

import java.util.List;

import com.schedule.app.domain.repository.GroupMeetingRepository;
import com.schedule.app.infrastructure.persistence.GroupMeetingJpaRepository;
import com.schedule.app.infrastructure.persistence.entity.GroupMeeting;

public class GroupMeetingRepositoryAdapter implements GroupMeetingRepository {
    private final GroupMeetingJpaRepository groupMeetingJpaRepository;

    public GroupMeetingRepositoryAdapter(GroupMeetingJpaRepository groupMeetingJpaRepository) {
        this.groupMeetingJpaRepository = groupMeetingJpaRepository;
    }

    @Override
    public GroupMeeting save(GroupMeeting groupMeeting) {
        return groupMeetingJpaRepository.save(groupMeeting);
    }

    @Override
    public List<GroupMeeting> findAll() {
        return groupMeetingJpaRepository.findAll();
    }

    @Override
    public List<GroupMeeting> findByName(String name) {
        return groupMeetingJpaRepository.findByName(name);
    }
}
