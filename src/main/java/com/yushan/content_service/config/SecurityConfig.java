package com.yushan.content_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.yushan.content_service.security.CustomMethodSecurityExpressionHandler;
import com.yushan.content_service.security.JwtAuthenticationEntryPoint;
import com.yushan.content_service.security.JwtAuthenticationFilter;

/**
 * Security Configuration for Content Service.
 */
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Security filter chain configuration
     * @param http HttpSecurity builder
     * @return SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints
            .csrf(csrf -> csrf.disable())
            
            // Configure session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Configure authorization
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/api/v1/health").permitAll()
                
                // Swagger/OpenAPI endpoints
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Test endpoints (only for development)
                .requestMatchers("/api/test/**").permitAll()
                
                // Static resources - allow public access to uploaded images
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/static/**").permitAll()
                
                // CORS preflight requests - allow OPTIONS for all endpoints
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                
                // Category APIs - public read, admin write (following yushan-backend pattern)
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers("/api/v1/categories/**").hasRole("ADMIN")
                
                // Novel APIs - following yushan-backend pattern
                .requestMatchers(HttpMethod.POST, "/api/v1/novels").hasAnyRole("AUTHOR","ADMIN")  // Create novel
                .requestMatchers(HttpMethod.GET, "/api/v1/novels").permitAll()      // List novels
                .requestMatchers(HttpMethod.GET, "/api/v1/novels/*").permitAll()    // Get novel by ID
                .requestMatchers(HttpMethod.PUT, "/api/v1/novels/*").authenticated() // Update novel
                .requestMatchers(HttpMethod.DELETE, "/api/v1/novels/*").authenticated() // Delete novel
                
                // Public novel endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/novels/*/view").permitAll() // Increment view
                .requestMatchers(HttpMethod.GET, "/api/v1/novels/count").permitAll()   // Get count
                .requestMatchers(HttpMethod.GET, "/api/v1/novels/category/**").permitAll() // Get by category
                .requestMatchers(HttpMethod.GET, "/api/v1/novels/uuid/**").permitAll() // Get by UUID
                .requestMatchers(HttpMethod.GET, "/api/v1/novels/author/**").permitAll() // Get by author
                
                // Admin endpoints - require admin role
                .requestMatchers("/api/v1/novels/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Disable form login and basic auth
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication manager bean
     * 
     * @param config Authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if configuration error
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Custom method security expression handler
     * @return MethodSecurityExpressionHandler instance
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new CustomMethodSecurityExpressionHandler();
    }
}
