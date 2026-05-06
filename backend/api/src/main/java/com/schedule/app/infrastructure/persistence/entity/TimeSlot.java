package com.schedule.app.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    private Instant startTime;
    private Instant endTime;

    public boolean overlaps(TimeSlot other) {
        if (other == null || other.getStartTime() == null || other.getEndTime() == null) {
            return false;
        }
        return this.startTime.isBefore(other.getEndTime()) && this.endTime.isAfter(other.getStartTime());
    }
}
