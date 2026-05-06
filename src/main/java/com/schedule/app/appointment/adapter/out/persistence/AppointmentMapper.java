package com.schedule.app.appointment.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.schedule.app.appointment.domain.model.Appointment;

@Component
public class AppointmentMapper {

    public AppointmentEntity toEntity(Appointment appointment) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(appointment.getId());
        entity.setName(appointment.getName());
        entity.setLocation(appointment.getLocation());
        entity.setMeetingDate(appointment.getMeetingDate());
        entity.setStartHour(appointment.getStartHour());
        entity.setEndHour(appointment.getEndHour());
        entity.setTypeAppointment(appointment.getTypeAppointment());
        entity.setCreatedAt(appointment.getCreatedAt());
        entity.setUpdatedAt(appointment.getUpdatedAt());
        return entity;
    }

    public Appointment toDomain(AppointmentEntity entity) {
        return new Appointment(
                entity.getId(),
                entity.getName(),
                entity.getLocation(),
                entity.getMeetingDate(),
                entity.getStartHour(),
                entity.getEndHour(),
                entity.getTypeAppointment(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
