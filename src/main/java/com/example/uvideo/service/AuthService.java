package com.example.uvideo.service;

import com.example.uvideo.dto.UserDTO;
import com.example.uvideo.entity.Token;
import com.example.uvideo.entity.User;
import com.example.uvideo.exceptions.GlobalException;
import com.example.uvideo.repository.TokenRepository;
import com.example.uvideo.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private TokenRepository tokenRepository;

    public String refresh(Cookie[] cookies) {
        String refreshToken = jwtService.getTokenFromCookie(cookies, "refreshToken");

        if (refreshToken == null) {
            throw new GlobalException("No refresh token");
        }

        Claims claims = jwtService.validateToken(refreshToken);
        String userData = claims.getSubject();

        return jwtService.generateAccessToken(userData);
    }

    public ResponseCookie logout(String token) {
        Optional<Token> tokenOpt = tokenRepository.findTokenByRefreshToken(token);
        if(tokenOpt.isEmpty()) {
            throw new GlobalException("Invalid token");
        }
        tokenRepository.delete(tokenOpt.get());

        return ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
    }

    public Map<String, String> createTokens(UserDTO userDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String userData = objectMapper.writeValueAsString(userDTO);

        String accessToken = jwtService.generateAccessToken(userData);
        String refreshToken = jwtService.generateRefreshToken(userData);

        String refreshCookie = jwtService.createRefreshCookie(refreshToken);

        Map<String, String> tokens = new HashMap<>();

        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("refreshCookie", refreshCookie);

        return tokens;
    }

    public void updateToken (UserDTO userDTO, Map<String, String> tokens) {
        Optional<Token> existingToken = tokenRepository.findTokenByUserId(userDTO.getId());
        if (existingToken.isPresent()) {
            existingToken.get().setRefreshToken(tokens.get("refreshToken"));
            tokenRepository.save(existingToken.get());
            return;
        }
        jwtService.saveRefreshToken(userDTO.getId(), tokens.get("refreshToken"));
    }

    public Long getUserIdFromAuthentication() throws JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userPrincipal = auth.getPrincipal().toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userNode = mapper.readTree(userPrincipal);
        return userNode.get("id").asLong();
    }
}