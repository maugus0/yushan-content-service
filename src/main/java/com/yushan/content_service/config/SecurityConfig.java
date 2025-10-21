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
import com.yushan.content_service.security.UserActivityFilter;

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
    
    @Autowired
    private UserActivityFilter userActivityFilter;

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
                .requestMatchers(HttpMethod.POST, "/api/v1/novels/batch/get").permitAll() // Batch get by IDs
                .requestMatchers(HttpMethod.GET, "/api/v1/novels/*/vote-count").permitAll() // Get vote count
                
                // Authenticated novel endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/novels/*/vote").authenticated() // Increment vote
                .requestMatchers(HttpMethod.PUT, "/api/v1/novels/*/rating").authenticated() // Update rating
                
                // Admin endpoints - require admin role
                .requestMatchers("/api/v1/novels/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/novels/*/unarchive").hasRole("ADMIN") // Unarchive novel
                
                // Search APIs - public read access
                .requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()
                
                // Chapter APIs - following yushan-backend pattern
                .requestMatchers(HttpMethod.POST, "/api/v1/chapters").hasAnyRole("AUTHOR","ADMIN")  // Create chapter
                .requestMatchers(HttpMethod.POST, "/api/v1/chapters/batch").hasAnyRole("AUTHOR","ADMIN")  // Batch create chapters
                .requestMatchers(HttpMethod.GET, "/api/v1/chapters/*").permitAll()    // Get chapter by UUID
                .requestMatchers(HttpMethod.PUT, "/api/v1/chapters").hasAnyRole("AUTHOR","ADMIN") // Update chapter
                .requestMatchers(HttpMethod.DELETE, "/api/v1/chapters/*").hasAnyRole("AUTHOR","ADMIN") // Delete chapter
                .requestMatchers(HttpMethod.PATCH, "/api/v1/chapters/publish").hasAnyRole("AUTHOR","ADMIN") // Publish chapter
                .requestMatchers(HttpMethod.PATCH, "/api/v1/chapters/novel/*/publish").hasAnyRole("AUTHOR","ADMIN") // Batch publish
                .requestMatchers(HttpMethod.DELETE, "/api/v1/chapters/novel/*").hasAnyRole("AUTHOR","ADMIN") // Delete all chapters
                
                // Public chapter endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/chapters/novel/*").permitAll() // Get chapters by novel
                .requestMatchers(HttpMethod.GET, "/api/v1/chapters/novel/*/number/*").permitAll() // Get chapter by novel ID and number
                .requestMatchers(HttpMethod.GET, "/api/v1/chapters/search").permitAll() // Search chapters
                .requestMatchers(HttpMethod.POST, "/api/v1/chapters/batch/get").permitAll() // Batch get by IDs
                .requestMatchers(HttpMethod.POST, "/api/v1/chapters/*/view").permitAll() // Increment view
                .requestMatchers(HttpMethod.GET, "/api/v1/chapters/*/next").permitAll() // Get next chapter
                .requestMatchers(HttpMethod.GET, "/api/v1/chapters/*/previous").permitAll() // Get previous chapter
                .requestMatchers(HttpMethod.GET, "/api/v1/chapters/exists").permitAll() // Check chapter existence
                
                // Author-only chapter endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/chapters/novel/*/statistics").hasAnyRole("AUTHOR","ADMIN") // Get statistics
                .requestMatchers(HttpMethod.GET, "/api/v1/chapters/novel/*/next-number").hasAnyRole("AUTHOR","ADMIN") // Get next chapter number
                
                // Admin-only chapter endpoints
                .requestMatchers(HttpMethod.DELETE, "/api/v1/chapters/admin/*").hasRole("ADMIN") // Admin delete chapter
                .requestMatchers(HttpMethod.DELETE, "/api/v1/chapters/admin/novel/*").hasRole("ADMIN") // Admin delete all chapters
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Disable form login and basic auth
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Add user activity filter after JWT filter
            .addFilterAfter(userActivityFilter, UsernamePasswordAuthenticationFilter.class);

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
