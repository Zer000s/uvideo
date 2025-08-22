package com.example.uvideo.repository;

import com.example.uvideo.entity.Token;
import com.example.uvideo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findTokenByRefreshToken(String refreshToken);

    Optional<Token> findTokenByUserId(Long userId);

    Optional<Token> removeTokenByRefreshToken(String refreshToken);
}