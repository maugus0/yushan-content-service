package com.yushan.content_service.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserActivityEvent(
        UUID userId,
        String serviceName,
        String endpoint,
        String method,
        LocalDateTime timestamp
) {}
