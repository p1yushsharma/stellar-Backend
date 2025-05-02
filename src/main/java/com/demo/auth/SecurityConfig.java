package com.demo.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  
            .authorizeRequests()
                .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()  
                .anyRequest().authenticated(); 
    }
}
