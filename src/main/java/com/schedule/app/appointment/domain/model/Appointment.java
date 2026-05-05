package com.schedule.app.appointment.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.schedule.app.appointment.domain.enums.TypeAppointment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    private Long id;
    private String name;
    private String location;
    private LocalDate meetingDate;
    private Integer startHour;
    private Integer endHour;
    private TypeAppointment typeAppointment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
