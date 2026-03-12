package com.khanabook.saas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        // For now, permit all to allow testing the sync APIs before JWT is fully
        // stitched in.
        // In production, this would be:
        // .authorizeHttpRequests(auth ->
        // auth.requestMatchers("/api/v1/sync/**").authenticated())
        // .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

    return http.build();
  }
}
