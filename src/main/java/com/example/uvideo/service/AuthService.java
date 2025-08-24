package com.example.uvideo.service;

import com.example.uvideo.dto.UserDTO;
import com.example.uvideo.entity.Token;
import com.example.uvideo.repository.TokenRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
}