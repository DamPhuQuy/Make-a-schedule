package com.schedule.app.entity;

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

    private Instant start_time;
    private Instant end_time;

    public boolean overlaps(TimeSlot other) {
        if (other == null || other.getStart_time() == null || other.getEnd_time() == null) {
            return false;
        }
        return this.start_time.isBefore(other.getEnd_time()) && this.end_time.isAfter(other.getStart_time());
    }
}
