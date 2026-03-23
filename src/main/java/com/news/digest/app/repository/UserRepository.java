package com.news.digest.app.repository;

import com.news.digest.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String userName);
    Boolean existsByEmail(String email);
    Boolean existsByUserName(String userName);
}

