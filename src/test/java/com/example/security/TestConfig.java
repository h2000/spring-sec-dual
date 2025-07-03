package com.example.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        // Return empty repository for tests to avoid OAuth2 configuration issues
        return new InMemoryClientRegistrationRepository();
    }
}