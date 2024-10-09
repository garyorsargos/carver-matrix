package com.fmc.carverApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fmc.carverApp.models.AppUser;

public interface UsersRepository extends JpaRepository<AppUser, Long> {

}