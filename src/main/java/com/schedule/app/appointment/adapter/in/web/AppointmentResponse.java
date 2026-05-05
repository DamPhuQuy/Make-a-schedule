package com.schedule.app.appointment.adapter.in.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.schedule.app.appointment.domain.enums.TypeAppointment;
import com.schedule.app.appointment.domain.model.Appointment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private String name;
    private String location;
    private LocalDate meetingDate;
    private Integer startHour;
    private Integer endHour;
    private TypeAppointment typeAppointment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AppointmentResponse fromDomain(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getName(),
                appointment.getLocation(),
                appointment.getMeetingDate(),
                appointment.getStartHour(),
                appointment.getEndHour(),
                appointment.getTypeAppointment(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
