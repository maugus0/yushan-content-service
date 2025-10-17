package com.yushan.content_service.security;

import com.yushan.content_service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * This filter runs before every request and:
 * 1. Extracts JWT token from Authorization header
 * 2. Validates the token
 * 3. Extracts user information from token
 * 4. Sets authentication in SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Filter method that processes each request
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. Extract token from Authorization header
            String token = extractTokenFromRequest(request);
            
            if (token != null && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                // 2. Extract user information from token
                String userId = jwtUtil.extractUserId(token);
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);
                Integer status = jwtUtil.extractStatus(token);
                
                // 3. Check if user is not already authenticated
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 4. Create CustomUserDetails from JWT claims
                    CustomUserDetails userDetails = new CustomUserDetails(userId, email, role, status);
                    
                    // 4.5. Check if user is enabled (not suspended/banned)
                    if (!userDetails.isEnabled()) {
                        // User is disabled, don't authenticate
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // 5. Create authentication object
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );
                    
                    // 6. Set additional details
                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // 7. Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the request
            logger.error("JWT authentication failed", e);
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     * 
     * @param request HTTP request
     * @return JWT token or null if not found
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
