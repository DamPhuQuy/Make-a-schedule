package com.schedule.app.appointment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, Long> {

    @Query("SELECT a FROM AppointmentEntity a WHERE a.name = :name AND a.typeAppointment = 'Nhóm' " +
           "AND a.meetingDate = :date AND a.startHour = :startHour AND a.endHour = :endHour")
    Optional<AppointmentEntity> findGroupAppointment(@Param("name") String name,
                                                     @Param("date") LocalDate date,
                                                     @Param("startHour") Integer startHour,
                                                     @Param("endHour") Integer endHour);

    @Query("SELECT a FROM AppointmentEntity a JOIN a.users u WHERE u.id = :userId " +
           "AND a.meetingDate = :date AND a.startHour = :startHour AND a.endHour = :endHour")
    Optional<AppointmentEntity> findUserAppointment(@Param("userId") Long userId,
                                                    @Param("date") LocalDate date,
                                                    @Param("startHour") Integer startHour,
                                                    @Param("endHour") Integer endHour);

    @Query("SELECT a FROM AppointmentEntity a JOIN a.users u WHERE u.id = :userId")
    List<AppointmentEntity> findByUserId(@Param("userId") Long userId);
}
