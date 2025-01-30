package com.fmc.starterApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fmc.starterApp.models.entity.AppUser;

public interface UsersRepository extends JpaRepository<AppUser, Long> {

}