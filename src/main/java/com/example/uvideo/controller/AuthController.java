package com.example.uvideo.controller;

import com.example.uvideo.dto.UserDTO;
import com.example.uvideo.entity.Token;
import com.example.uvideo.entity.User;
import com.example.uvideo.repository.TokenRepository;
import com.example.uvideo.repository.UserRepository;
import com.example.uvideo.service.AuthService;
import com.example.uvideo.service.JWTService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
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
    @Autowired
    private AuthService authService;

    @PostMapping("/registration")
    public ResponseEntity<Object> registration(@RequestBody @Valid User user) {
        try {
            Optional<User> existingUser = userRepository.findByPhone(user.getPhone());
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body("Phone is already registered");
            }
            userRepository.save(user);

            UserDTO userDTO = new UserDTO(user.getId(), user.getPhone(), user.getDisplayName());
            Map<String, String> tokens = authService.createTokens(userDTO);
            jwtService.saveRefreshToken(user.getId(), tokens.get("refreshToken"));

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, tokens.get("refreshCookie"))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.get("accessToken"))
                    .body(userDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid User user) {
        try {
            Optional<User> existingUser = userRepository.findByPhone(user.getPhone());
            if (existingUser.isEmpty()) {
                return ResponseEntity.badRequest().body("Phone is not registered");
            }

            UserDTO userDTO = new UserDTO(existingUser.get().getId(), existingUser.get().getPhone(), existingUser.get().getDisplayName());
            Map<String, String> tokens = authService.createTokens(userDTO);

            authService.updateToken(userDTO, tokens);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, tokens.get("refreshCookie"))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.get("accessToken"))
                    .body(userDTO);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        try {
            String refreshToken = jwtService.getTokenFromCookie(request.getCookies(), "refreshToken");

            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token");
            }

            Claims claims = jwtService.validateToken(refreshToken);
            String userData = claims.getSubject();

            String newAccessToken = jwtService.generateAccessToken(userData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                    .body("Access token refreshed");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Object> logout(@RequestParam @Valid String token) {
        try {
            Optional<Token> tokenOpt = tokenRepository.findTokenByRefreshToken(token);
            if(tokenOpt.isPresent()) {
                tokenRepository.delete(tokenOpt.get());

                ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                        .path("/")
                        .httpOnly(true)
                        .maxAge(0)
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                        .body("Logged out");
            }
            else {
                return ResponseEntity.badRequest().body("Invalid token");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}