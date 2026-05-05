package com.schedule.app.client.api;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schedule.app.client.dto.AppointmentDTO;
import com.schedule.app.client.dto.CreateAppointmentRequest;
import com.schedule.app.client.dto.UpdateAppointmentRequest;

public class AppointmentApiService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public AppointmentApiService() {
        this.apiClient = new ApiClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<AppointmentDTO> getAllAppointments() throws Exception {
        return apiClient.get("/appointments", new TypeReference<List<AppointmentDTO>>() {});
    }

    public AppointmentDTO getAppointmentById(Long id) throws Exception {
        return apiClient.get("/appointments/" + id, AppointmentDTO.class);
    }

    public List<AppointmentDTO> getAppointmentsByUserId(Long userId) throws Exception {
        return apiClient.get("/appointments/user/" + userId, new TypeReference<List<AppointmentDTO>>() {});
    }

    public AppointmentDTO createAppointment(CreateAppointmentRequest request) throws Exception {
        return apiClient.post("/appointments", request, AppointmentDTO.class);
    }

    public AppointmentDTO updateAppointment(Long id, UpdateAppointmentRequest request) throws Exception {
        return apiClient.put("/appointments/" + id, request, AppointmentDTO.class);
    }

    public void deleteAppointment(Long id) throws Exception {
        apiClient.delete("/appointments/" + id);
    }

    public void addUserToAppointment(Long appointmentId, Long userId) throws Exception {
        apiClient.post("/appointments/" + appointmentId + "/users/" + userId, null, Void.class);
    }

    public void addReminderToAppointment(Long appointmentId, Long reminderId) throws Exception {
        apiClient.post("/appointments/" + appointmentId + "/reminders/" + reminderId, null, Void.class);
    }
}
