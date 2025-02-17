package com.fmc.starterApp.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fmc.starterApp.models.entity.User2;

public interface User2Repository extends JpaRepository<User2, Long> {
    Optional<User2> findByKeycloakId(String keycloakId);
}
