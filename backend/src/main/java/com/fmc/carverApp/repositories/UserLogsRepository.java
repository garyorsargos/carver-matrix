package com.fmc.carverApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fmc.carverApp.models.UserLogs;

public interface UserLogsRepository extends JpaRepository<UserLogs, Long> {

}