package com.example.uvideo.controller;

import com.example.uvideo.dto.UserDTO;
import com.example.uvideo.entity.Token;
import com.example.uvideo.entity.User;
import com.example.uvideo.exceptions.GlobalException;
import com.example.uvideo.repository.TokenRepository;
import com.example.uvideo.repository.UserRepository;
import com.example.uvideo.service.AuthService;
import com.example.uvideo.service.JWTService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    @PostMapping("/registration")
    public ResponseEntity<Object> registration(@RequestBody @Valid User user) throws JsonProcessingException {
        Optional<User> existingUser = userRepository.findByPhone(user.getPhone());
        if (existingUser.isPresent()) {
            throw new GlobalException("Phone is already registered");
        }
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);

        UserDTO userDTO = new UserDTO(user.getId(), user.getPhone(), user.getDisplayName());
        Map<String, String> tokens = authService.createTokens(userDTO);
        jwtService.saveRefreshToken(user.getId(), tokens.get("refreshToken"));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokens.get("refreshCookie"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.get("accessToken"))
                .body(userDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid User user) throws JsonProcessingException {
        Optional<User> existingUser = userRepository.findByPhone(user.getPhone());
        if (existingUser.isEmpty()) {
            throw new GlobalException("Phone is not registered");
        }
        if (!passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {
            throw new GlobalException("Wrong password");
        }
        UserDTO userDTO = new UserDTO(existingUser.get().getId(), existingUser.get().getPhone(), existingUser.get().getDisplayName());
        Map<String, String> tokens = authService.createTokens(userDTO);

        authService.updateToken(userDTO, tokens);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokens.get("refreshCookie"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.get("accessToken"))
                .body(userDTO);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
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