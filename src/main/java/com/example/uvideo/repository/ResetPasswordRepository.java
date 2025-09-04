package com.example.uvideo.repository;

import com.example.uvideo.entity.ResetPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {
    Optional<ResetPassword> findByUserId(Long userId);

    Optional<ResetPassword> findByUserIdAndCode(Long userId, String code);
}