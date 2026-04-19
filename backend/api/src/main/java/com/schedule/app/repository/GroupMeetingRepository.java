package com.schedule.app.repository;

import com.schedule.app.entity.GroupMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMeetingRepository extends JpaRepository<GroupMeeting, Long> {
    List<GroupMeeting> findByName(String name);
}
