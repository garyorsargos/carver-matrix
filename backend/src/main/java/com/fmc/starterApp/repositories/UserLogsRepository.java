package com.fmc.starterApp.repositories;

import com.fmc.starterApp.models.UserLogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLogsRepository extends JpaRepository<UserLogs, Long> {

}