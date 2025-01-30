package com.fmc.starterApp.models.dto;

import lombok.Builder;
import lombok.Data;
import com.fmc.starterApp.models.entity.AppUser;

import java.util.List;

@Builder
@Data
public class AdminDTO {
    List<AppUser> users;
    int totalUsers;
}
