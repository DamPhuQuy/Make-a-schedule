package com.schedule.app.application.usecase;

import com.schedule.app.application.dto.request.RegisterRequest;
import com.schedule.app.application.dto.response.MessageResponse;

public interface RegisterUseCase {
    MessageResponse register(RegisterRequest request);
}
