package com.demo.auth.controller;

import com.demo.auth.dto.LoginRequest;
import com.demo.auth.dto.SignupRequest;
import com.demo.auth.entity.User;
import com.demo.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            userService.signup(request);
            return ResponseEntity.ok("Signup successful");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());  // 409 Conflict
        }
    }
    

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    try {
        
        User user = userService.login(request);
        
      
        return ResponseEntity.ok(user);  
        
    } catch (RuntimeException e) {
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body("Invalid credentials: " + e.getMessage()); 
    }
}
}