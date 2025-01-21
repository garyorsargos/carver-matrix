package com.fmc.starterApp.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class User2DTO {
    List<User2> users;
    int totalUsers;
}
