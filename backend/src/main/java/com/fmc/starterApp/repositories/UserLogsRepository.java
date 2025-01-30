package com.fmc.starterApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fmc.starterApp.models.entity.UserLogs;

public interface UserLogsRepository extends JpaRepository<UserLogs, Long> {

}