package com.schedule.app.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupMeeting extends Base {

    @ManyToMany
    @JoinTable(
      name = "group_meeting_participants",
      joinColumns = @JoinColumn(name = "group_meeting_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> participants;

}
