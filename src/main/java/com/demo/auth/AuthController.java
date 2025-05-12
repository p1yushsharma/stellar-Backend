package com.demo.auth.controller;
import com.demo.auth.entity.User;
import com.demo.auth.dto.JwtResponse;
import com.demo.auth.entity.RefreshToken;
import com.demo.auth.repository.RefreshTokenRepository;
import com.demo.auth.service.RefreshTokenService;
import com.demo.auth.dto.LoginRequest;
import com.demo.auth.dto.SignupRequest;
import com.demo.auth.service.UserService;
import com.demo.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    @Lazy
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            userService.signup(request);
            return ResponseEntity.ok("Signup successful");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            RefreshToken refreshTokenObject = refreshTokenService.createRefreshToken(userDetails.getUsername());
            String refreshToken = refreshTokenObject.getToken(); 

            JwtResponse jwtResponse = JwtResponse.builder()
                    .accessToken(jwt)
                    .refreshToken(refreshToken)
                    .build();
            return ResponseEntity.ok(jwtResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials: " + e.getMessage());
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshToken refreshTokenRequest) {
        try {
           
            RefreshToken refreshTokenFromDb = refreshTokenService.findByToken(refreshTokenRequest.getToken())
                    .orElseThrow(() -> new RuntimeException("Refresh Token is not in the database!"));

           
            if (refreshTokenService.verifyExpiration(refreshTokenFromDb)) {
                User user = refreshTokenFromDb.getUser();
                String newAccessToken = jwtUtil.generateToken(user.getEmail());

                JwtResponse jwtResponse = JwtResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshTokenRequest.getToken()) 

                        .build();

                return ResponseEntity.ok(jwtResponse);
            } else {
                throw new RuntimeException("Refresh token has expired");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error refreshing token: " + e.getMessage());
        }
    }
    @PostMapping("/logout")
     public ResponseEntity<?> logout(@RequestBody RefreshToken refreshTokenRequest) {
     try {
        
        RefreshToken refreshTokenFromDb = refreshTokenService.findByToken(refreshTokenRequest.getToken())
                .orElseThrow(() -> new RuntimeException("Refresh Token not found"));

      
        refreshTokenService.delete(refreshTokenFromDb);

       
        return ResponseEntity.ok("Logged out successfully");
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error logging out: " + e.getMessage());
    }
}

}
 