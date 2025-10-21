package com.yushan.content_service.security;

import com.yushan.content_service.dto.event.UserActivityEvent;
import com.yushan.content_service.service.KafkaEventProducerService;
import com.yushan.content_service.util.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class UserActivityFilter extends OncePerRequestFilter {

    @Autowired
    private KafkaEventProducerService kafkaEventProducerService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            log.info("UserActivityFilter triggered for: {}", request.getRequestURI());
            
            // Only track authenticated requests
            if (isAuthenticatedRequest(request)) {
                log.info("Request is authenticated");
                UUID userId = SecurityUtils.getCurrentUserId();
                log.info("User ID from SecurityUtils: {}", userId);
                
                if (userId != null) {
                    UserActivityEvent event = new UserActivityEvent(
                            userId,
                            "content-service",
                            request.getRequestURI(),
                            request.getMethod(),
                            LocalDateTime.now()
                    );
                    
                    kafkaEventProducerService.publishUserActivityEvent(event);
                    log.info("Tracked user activity: userId={}, endpoint={}", userId, request.getRequestURI());
                } else {
                    log.warn("User ID is null");
                }
            } else {
                log.info("Request is not authenticated");
            }
        } catch (Exception e) {
            log.warn("Failed to track user activity: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticatedRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ");
    }
}
