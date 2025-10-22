package com.yushan.content_service.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private ByteArrayOutputStream outputStream;
    private StringWriter stringWriter;

    @BeforeEach
    void setUp() throws IOException {
        outputStream = new ByteArrayOutputStream();
        stringWriter = new StringWriter();
        
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }
            
            @Override
            public boolean isReady() {
                return true;
            }
            
            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            }
        });
        
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    }

    @Test
    void testCommence_WithNullException() throws IOException, ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, null);

        // Then
        verify(response).setContentType("application/json");
        verify(response).setStatus(401);
        
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"success\":false"));
        assertTrue(responseBody.contains("\"message\":\"Unauthorized: Invalid or missing JWT token\""));
        assertTrue(responseBody.contains("\"error\":\"UNAUTHORIZED\""));
        assertTrue(responseBody.contains("\"status\":401"));
        assertTrue(responseBody.contains("\"path\":\"/api/test\""));
        assertTrue(responseBody.contains("\"timestamp\""));
    }

    @Test
    void testCommence_WithBadCredentialsException() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new BadCredentialsException("Invalid credentials");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(response).setContentType("application/json");
        verify(response).setStatus(401);
        
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"details\":\"Invalid credentials provided\""));
    }

    @Test
    void testCommence_WithAccountExpiredException() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new AccountExpiredException("Account expired");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"details\":\"Account has expired\""));
    }

    @Test
    void testCommence_WithAccountLockedException() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new AuthenticationException("Account locked") {};
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"details\":\"Authentication failed: Account locked\""));
    }

    @Test
    void testCommence_WithDisabledException() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new DisabledException("Account disabled");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"details\":\"Account is disabled\""));
    }

    @Test
    void testCommence_WithCredentialsExpiredException() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new CredentialsExpiredException("Credentials expired");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"details\":\"Credentials have expired\""));
    }

    @Test
    void testCommence_WithInsufficientAuthenticationException() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new InsufficientAuthenticationException("Insufficient auth");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"details\":\"Insufficient authentication information\""));
    }

    @Test
    void testCommence_WithAuthenticationCredentialsNotFoundException() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new AuthenticationException("Credentials not found") {};
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"details\":\"Authentication failed: Credentials not found\""));
    }

    @Test
    void testCommence_WithCustomException() throws IOException, ServletException {
        // Given
        AuthenticationException authException = new AuthenticationException("Custom error") {};
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"details\":\"Authentication failed: Custom error\""));
    }

    @Test
    void testCommence_WithIOException() throws IOException, ServletException {
        // Given
        when(response.getOutputStream()).thenThrow(new IOException("IO Error"));
        when(request.getRequestURI()).thenReturn("/api/test");

        // When & Then
        assertThrows(IOException.class, () -> {
            jwtAuthenticationEntryPoint.commence(request, response, null);
        });
    }

    @Test
    void testCommence_ResponseStructure() throws IOException, ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/novels");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, null);

        // Then
        String responseBody = outputStream.toString();
        
        // Verify JSON structure
        assertTrue(responseBody.contains("\"success\""));
        assertTrue(responseBody.contains("\"message\""));
        assertTrue(responseBody.contains("\"error\""));
        assertTrue(responseBody.contains("\"status\""));
        assertTrue(responseBody.contains("\"path\""));
        assertTrue(responseBody.contains("\"timestamp\""));
        
        // Verify specific values
        assertTrue(responseBody.contains("\"success\":false"));
        assertTrue(responseBody.contains("\"status\":401"));
        assertTrue(responseBody.contains("\"path\":\"/api/v1/novels\""));
    }
}
