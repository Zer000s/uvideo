package com.example.uvideo.repository;

import com.example.uvideo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

    Optional<User> findByPassword(String password);

    Optional<User> findByEmail(String email);
}