package com.khanabook.saas.security;

import com.khanabook.saas.utility.JwtUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            try {
                if (!jwtUtility.isTokenExpired(jwt)) {
                    Long restaurantId = jwtUtility.extractRestaurantId(jwt);
                    // Inject Tenant ID into the Secure Thread Context
                    TenantContext.setCurrentTenant(restaurantId);
                }
            } catch (Exception e) {
                // Token invalid or malformed
                System.err.println("JWT Validation Failed: " + e.getMessage());
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // CRITICAL: Always clear ThreadLocal to prevent memory leaks or data bleeding
            TenantContext.clear();
        }
    }
}
