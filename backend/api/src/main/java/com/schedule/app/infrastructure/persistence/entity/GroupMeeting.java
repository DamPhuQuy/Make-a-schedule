package com.schedule.app.infrastructure.persistence.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("GROUP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupMeeting extends Appointment {

    @OneToMany(mappedBy = "groupMeeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMeetingParticipant> participants = new ArrayList<>();

}
