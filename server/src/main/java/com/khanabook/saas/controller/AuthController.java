package com.khanabook.saas.controller;

import com.khanabook.saas.utility.JwtUtility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtility jwtUtility;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("\n[AuthController] Login Request received for: " + request.getEmail() + " from device: "
                + request.getDeviceId());

        // In a real app, verify password here. For sync test, we generate a real JWT.
        String token = jwtUtility.generateToken(request.getEmail(), 1L);
        System.out.println("[AuthController] JWT Token successfully generated. Logging in as Admin User.");
        return ResponseEntity.ok(new AuthResponse(token, 1L, "Admin User"));
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        System.out.println("\n[AuthController] Google Login Request received from device: " + request.getDeviceId());
        // For sync test, generate real JWT from Google ID token placeholder
        String token = jwtUtility.generateToken("google_user", 1L);
        return ResponseEntity.ok(new AuthResponse(token, 1L, "Google User"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        System.out.println("\n[AuthController] Signup Request received for: " + request.getEmail());

        // In a real app, you would create the tenant, user, and restaurant profile
        // here.
        // For this demo/test, we give them a mocked tenant/restaurant ID and a valid
        // token.
        Long newRestaurantId = System.currentTimeMillis() % 10000;
        String token = jwtUtility.generateToken(request.getEmail(), newRestaurantId);

        System.out.println("[AuthController] JWT Token successfully generated for new user.");
        return ResponseEntity.ok(new AuthResponse(token, newRestaurantId, request.getName()));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        private String email;
        private String passwordHash;
        private String deviceId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GoogleLoginRequest {
        private String idToken;
        private String deviceId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignupRequest {
        private String email;
        private String name;
        private String password;
        private String deviceId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthResponse {
        private String token;
        private Long restaurantId;
        private String userName;
    }
}
