package com.bughunters.pos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary, permissive security configuration.
 *
 * <p><strong>PLACEHOLDER — TO BE REPLACED BY MEMBER 1 (SHARON).</strong>
 * This configuration disables CSRF and permits all requests so the hotel
 * module can be developed and tested before the JWT filter chain is wired.
 * Sharon's Auth module will replace this bean with the full JWT-secured chain.
 *
 * <p>Do NOT use in production.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
