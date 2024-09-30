package com.fmc.starterApp.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class AdminDTO {
    List<AppUser> users;
    int totalUsers;
}
