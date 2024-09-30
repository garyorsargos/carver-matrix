package com.fmc.starterApp.models.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String jwtSecret;
    private int jwtExpirationMs;
    private String jwtCookie;
}
