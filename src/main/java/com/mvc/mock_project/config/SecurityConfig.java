package com.mvc.mock_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable for development or REST APIs
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/api/public/**", "/auth/**", "/login", "/register").permitAll()
                
                // Role-based URL mappings according to SRS
                // Customer
                .requestMatchers("/api/bookings/**", "/api/reviews/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_ADMIN")
                
                // Court Owner
                .requestMatchers("/api/owner/**", "/api/facilities/manage/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                
                // Staff
                .requestMatchers("/api/staff/**", "/api/ops/**").hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN")
                
                // Admin
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                
                // Any other requests require authentication
                .anyRequest().authenticated()
            )
            // Form login for standard web app
            .formLogin(form -> form
                .permitAll() // Allows access to default /login page or custom login page
            )
            .logout(logout -> logout
                .permitAll()
            );
            
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
