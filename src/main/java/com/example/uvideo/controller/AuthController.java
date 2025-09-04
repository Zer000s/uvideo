package com.example.uvideo.controller;

import com.example.uvideo.dto.GenerateResetCodeRequest;
import com.example.uvideo.dto.ResetPasswordRequest;
import com.example.uvideo.dto.UserDTO;
import com.example.uvideo.entity.ResetPassword;
import com.example.uvideo.entity.User;
import com.example.uvideo.exceptions.GlobalException;
import com.example.uvideo.repository.ResetPasswordRepository;
import com.example.uvideo.repository.UserRepository;
import com.example.uvideo.service.AuthService;
import com.example.uvideo.service.JWTService;
import com.example.uvideo.service.MailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ResetPasswordRepository resetPasswordRepository;
    @Autowired
    private MailService mailService;

    @PostMapping("/registration")
    public ResponseEntity<Object> registration(@RequestBody @Valid User user) throws JsonProcessingException {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new GlobalException("Email is already registered");
        }
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);

        UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getDisplayName());
        Map<String, String> tokens = authService.createTokens(userDTO);
        jwtService.saveRefreshToken(user.getId(), tokens.get("refreshToken"));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokens.get("refreshCookie"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.get("accessToken"))
                .body(userDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid User user) throws JsonProcessingException {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isEmpty()) {
            throw new GlobalException("Email is not registered");
        }
        if (!passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
            throw new GlobalException("Wrong password");
        }
        UserDTO userDTO = new UserDTO(existingUser.get().getId(), existingUser.get().getEmail(), existingUser.get().getDisplayName());
        Map<String, String> tokens = authService.createTokens(userDTO);

        authService.updateToken(userDTO, tokens);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokens.get("refreshCookie"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.get("accessToken"))
                .body(userDTO);
    }

    @PostMapping("/generate_code")
    public ResponseEntity<Object> generateCode(@RequestBody @Valid GenerateResetCodeRequest request) {
        String code = UUID.randomUUID().toString();

        Optional<ResetPassword> existingResetPassword = resetPasswordRepository.findByUserId(request.getUserId());

        if(existingResetPassword.isPresent()) {
            throw new GlobalException("Try later");
        }

        //mailService.sendPasswordReset(request.getEmail(), code);

        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setCode(code);
        resetPassword.setUserId(request.getUserId());
        resetPasswordRepository.save(resetPassword);

        return ResponseEntity.ok("Code sent to " + request.getEmail());
    }

    @PostMapping("/reset")
    public ResponseEntity<Object> reset(@RequestBody @Valid ResetPasswordRequest request) {
        Optional<ResetPassword> existingResetPassword =
                resetPasswordRepository.findByUserIdAndCode(request.getUserId(), request.getCode());

        if (existingResetPassword.isEmpty()) {
            throw new GlobalException("Code not available");
        }

        Instant createdAt = existingResetPassword.get().getCreatedAt();
        Instant expirationTime = createdAt.plus(Duration.ofMinutes(5));
        if (Instant.now().isAfter(expirationTime)) {
            throw new GlobalException("Reset code expired");
        }

        Optional<User> existingUser = userRepository.findById(request.getUserId());
        if (existingUser.isEmpty()) {
            throw new GlobalException("User not found");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        existingUser.get().setPassword(hashedPassword);
        userRepository.save(existingUser.get());

        return ResponseEntity.ok("Password reset");
    }

    @GetMapping("/refresh")
    public ResponseEntity<Object> refresh(HttpServletRequest request) {
        String newAccessToken = authService.refresh(request.getCookies());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                .body("Access token refreshed");
    }

    @GetMapping("/logout")
    public ResponseEntity<Object> logout(@RequestParam @Valid String token) {
        ResponseCookie deleteCookie = authService.logout(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body("Logged out");
    }
}