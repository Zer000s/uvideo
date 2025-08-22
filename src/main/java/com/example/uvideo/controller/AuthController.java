package com.example.uvideo.controller;

import com.example.uvideo.entity.Token;
import com.example.uvideo.entity.User;
import com.example.uvideo.dto.UserDTO;
import com.example.uvideo.repository.TokenRepository;
import com.example.uvideo.repository.UserRepository;
import com.example.uvideo.service.JWTService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    private TokenRepository tokenRepository;

    @PostMapping("/registration")
    public ResponseEntity<Object> registration(@Valid @RequestBody User user) {
        try {
            Optional<User> existingUser = userRepository.findByPhone(user.getPhone());
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body("Phone is already registered");
            }
            userRepository.save(user);

            UserDTO userDTO = new UserDTO(user.getId(), user.getPhone(), user.getDisplayName());
            //create JSON-string
            ObjectMapper objectMapper = new ObjectMapper();
            String userData = objectMapper.writeValueAsString(userDTO);

            String token = jwtService.generateToken(userData);
            jwtService.saveTokenInDB(user.getId(), token);
            String cookie = jwtService.createCookie(token);

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie).body(userDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Логин по телефону + паролю
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody User user) {
        try {
            Optional<User> existingUser = userRepository.findByPhone(user.getPhone());
            if (existingUser.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            String token = jwtService.generateToken(existingUser.get().getId().toString());
            String cookie = jwtService.createCookie(token);
            UserDTO userDTO = new UserDTO(user.getId(), user.getPhone(), user.getDisplayName());

            Optional<Token> existingToken = tokenRepository.findTokenByUserId(existingUser.get().getId());
            if (existingToken.isEmpty()) {
                jwtService.saveTokenInDB(existingUser.get().getId(), token);
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie).body(userDTO);
            }
            existingToken.get().setRefreshToken(token);
            tokenRepository.save(existingToken.get());
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie).body(userDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Обновление токена
    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("jwt") String refreshToken) {
        try {
            if(refreshToken == null || refreshToken.isEmpty()) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            Claims tokenData = jwtService.validateToken(refreshToken);
            Optional<Token> tokenIsDB = tokenRepository.findTokenByRefreshToken(refreshToken);
            String userId = tokenData.getSubject();
            if(tokenIsDB.isEmpty() || userId == null) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            String token = jwtService.generateToken(userId);
            String cookie = jwtService.createCookie(token);
            Optional<Token> existingToken = tokenRepository.findTokenByUserId(Long.parseLong(userId));
            if (existingToken.isEmpty()) {
                jwtService.saveTokenInDB(Long.parseLong(userId), token);
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie).body("Success");
            }
            existingToken.get().setRefreshToken(token);
            tokenRepository.save(existingToken.get());
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie).body("Success");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Выход — удаление токена из БД
    @GetMapping("/logout")
    public ResponseEntity<Object> logout(@RequestParam String token) {
        try {
            Optional<Token> tokenOpt = tokenRepository.findTokenByRefreshToken(token);
            if(tokenOpt.isPresent()) {
                tokenRepository.delete(tokenOpt.get());
                return ResponseEntity.ok("Logged out");
            } else {
                return ResponseEntity.badRequest().body("Invalid token");
            }
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}