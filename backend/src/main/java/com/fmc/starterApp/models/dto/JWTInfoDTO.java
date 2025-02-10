package com.fmc.starterApp.models.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class JWTInfoDTO {
    String keycloakID;
    String sessionID;
    boolean emailVerified;
    String username;
    String email;
    List<String> starterAppRole;
    List<String> accountRole;
}
