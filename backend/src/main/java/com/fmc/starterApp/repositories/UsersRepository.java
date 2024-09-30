package com.fmc.starterApp.repositories;

import com.fmc.starterApp.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<AppUser, Long> {

}