package com.fmc.starterApp.models.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import com.fmc.starterApp.models.entity.User2;

@Builder
@Data
public class User2DTO {
    List<User2> users;
    int totalUsers;
}
