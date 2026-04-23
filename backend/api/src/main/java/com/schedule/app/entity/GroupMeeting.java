package com.schedule.app.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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

    @OneToMany(mappedBy = "groupMeeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMeetingParticipant> participants = new ArrayList<>();

}
