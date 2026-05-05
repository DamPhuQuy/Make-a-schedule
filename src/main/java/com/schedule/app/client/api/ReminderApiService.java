package com.schedule.app.client.api;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schedule.app.client.dto.ReminderDTO;

public class ReminderApiService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public ReminderApiService() {
        this.apiClient = new ApiClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<ReminderDTO> getAllReminders() throws Exception {
        String response = apiClient.get("/reminders", String.class);
        return objectMapper.readValue(response, new TypeReference<List<ReminderDTO>>() {});
    }

    public ReminderDTO getReminderById(Long id) throws Exception {
        return apiClient.get("/reminders/" + id, ReminderDTO.class);
    }
}
