package com.mvc.mock_project.config;

import com.mvc.mock_project.security.CustomOAuth2UserService;
import com.mvc.mock_project.security.JwtAuthenticationFilter;
import com.mvc.mock_project.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable for development or REST APIs
            .authorizeHttpRequests(auth -> auth
                // Public endpoints & static resources
                .requestMatchers(
                    "/", 
                    "/api/public/**", 
                    "/api/auth/**",
                    "/auth/**", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/webjars/**"
                ).permitAll()
                
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
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .userInfoEndpoint(info -> info
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2LoginSuccessHandler)
            )
            .logout(logout -> logout
                .permitAll()
            );

        http.addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
