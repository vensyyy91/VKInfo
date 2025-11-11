package ru.vensy.vkinfo.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vensy.vkinfo.auth.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}