package com.fmc.carverApp.models.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String jwtSecret;
    private int jwtExpirationMs;
    private String jwtCookie;
}
