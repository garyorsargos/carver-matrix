package com.fmc.starterApp.services;


import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.fmc.starterApp.models.dto.User2DTO;
import com.fmc.starterApp.models.entity.User2;
import com.fmc.starterApp.repositories.User2Repository;

public class User2Service {
    private final User2Repository user2Repository;

    public User2Service(User2Repository user2Repository) {
        this.user2Repository = user2Repository;
    }

    @Transactional
    public User2 insertNewUser(User2 user2) {
        user2Repository.save(user2);
        return user2;
    }

    public User2DTO getUserInfo() {
        List<User2> users = user2Repository.findAll();
        return User2DTO.builder()
                .users(users)
                .totalUsers(users.size())
                .build();
    }
}
