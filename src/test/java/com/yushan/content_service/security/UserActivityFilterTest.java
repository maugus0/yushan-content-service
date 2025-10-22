package com.yushan.content_service.security;

import com.yushan.content_service.dto.event.UserActivityEvent;
import com.yushan.content_service.service.KafkaEventProducerService;
import com.yushan.content_service.util.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivityFilterTest {

    @Mock
    private KafkaEventProducerService kafkaEventProducerService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private UserActivityFilter userActivityFilter;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    @Test
    void testDoFilterInternal_WithAuthenticatedRequest() throws ServletException, IOException {
        // Given
        String requestUri = "/api/v1/novels";
        String method = "GET";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getMethod()).thenReturn(method);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            // When
            userActivityFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            verify(kafkaEventProducerService).publishUserActivityEvent(any(UserActivityEvent.class));
        }
    }

    @Test
    void testDoFilterInternal_WithNonAuthenticatedRequest() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/novels");

        // When
        userActivityFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(kafkaEventProducerService, never()).publishUserActivityEvent(any(UserActivityEvent.class));
    }

    @Test
    void testDoFilterInternal_WithInvalidAuthHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");
        when(request.getRequestURI()).thenReturn("/api/v1/novels");

        // When
        userActivityFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(kafkaEventProducerService, never()).publishUserActivityEvent(any(UserActivityEvent.class));
    }

    @Test
    void testDoFilterInternal_WithKafkaException() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(request.getRequestURI()).thenReturn("/api/v1/novels");
        when(request.getMethod()).thenReturn("GET");

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);
            doThrow(new RuntimeException("Kafka error")).when(kafkaEventProducerService)
                    .publishUserActivityEvent(any(UserActivityEvent.class));

            // When
            userActivityFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            // Should not throw exception, just log warning
        }
    }

    @Test
    void testDoFilterInternal_WithSecurityUtilsException() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(request.getRequestURI()).thenReturn("/api/v1/novels");

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                    .thenThrow(new RuntimeException("SecurityUtils error"));

            // When
            userActivityFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            verify(kafkaEventProducerService, never()).publishUserActivityEvent(any(UserActivityEvent.class));
        }
    }

    @Test
    void testDoFilterInternal_VerifyEventContent() throws ServletException, IOException {
        // Given
        String requestUri = "/api/v1/chapters/123";
        String method = "POST";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(request.getRequestURI()).thenReturn(requestUri);
        when(request.getMethod()).thenReturn(method);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            // When
            userActivityFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(kafkaEventProducerService).publishUserActivityEvent(argThat(event -> {
                UserActivityEvent userEvent = (UserActivityEvent) event;
                return testUserId.equals(userEvent.userId()) &&
                       "content-service".equals(userEvent.serviceName()) &&
                       requestUri.equals(userEvent.endpoint()) &&
                       method.equals(userEvent.method()) &&
                       userEvent.timestamp() != null;
            }));
        }
    }

    @Test
    void testDoFilterInternal_WithEmptyBearerToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(request.getRequestURI()).thenReturn("/api/v1/novels");

        // When
        userActivityFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(kafkaEventProducerService, never()).publishUserActivityEvent(any(UserActivityEvent.class));
    }

    @Test
    void testDoFilterInternal_WithBearerOnly() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer");
        when(request.getRequestURI()).thenReturn("/api/v1/novels");

        // When
        userActivityFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(kafkaEventProducerService, never()).publishUserActivityEvent(any(UserActivityEvent.class));
    }

    @Test
    void testDoFilterInternal_WithDifferentHttpMethods() throws ServletException, IOException {
        // Given
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
        
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(request.getRequestURI()).thenReturn("/api/v1/novels");

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            for (String method : methods) {
                when(request.getMethod()).thenReturn(method);

                // When
                userActivityFilter.doFilterInternal(request, response, filterChain);

                // Then
                verify(kafkaEventProducerService).publishUserActivityEvent(argThat(event -> {
                    UserActivityEvent userEvent = (UserActivityEvent) event;
                    return method.equals(userEvent.method());
                }));
            }
        }
    }

    @Test
    void testDoFilterInternal_WithFilterChainException() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(request.getRequestURI()).thenReturn("/api/v1/novels");
        doThrow(new ServletException("Filter chain error")).when(filterChain).doFilter(request, response);

        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

            // When & Then
            assertThrows(ServletException.class, () -> {
                userActivityFilter.doFilterInternal(request, response, filterChain);
            });
        }
    }
}
