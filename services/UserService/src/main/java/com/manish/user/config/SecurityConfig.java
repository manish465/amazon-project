package com.manish.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("**/create").permitAll()
                                .anyRequest().authenticated()
                ).oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(Customizer.withDefaults())
                );

        return httpSecurity.build();
    }
}
