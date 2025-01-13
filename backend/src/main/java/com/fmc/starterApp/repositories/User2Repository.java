package com.fmc.starterApp.repositories;

import com.fmc.starterApp.models.User2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface User2Repository extends JpaRepository<User2, Long> {
}
