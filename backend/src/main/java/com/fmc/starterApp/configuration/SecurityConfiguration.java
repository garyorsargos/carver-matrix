package com.fmc.starterApp.configuration;

import com.fmc.starterApp.configuration.security.CustomAccessDeniedHandler;
import com.fmc.starterApp.configuration.security.JwtAuthConverter;
import com.fmc.starterApp.configuration.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
@Configuration()
@EnableMethodSecurity
public class SecurityConfiguration {

    @Autowired
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    public static final String STARTER_ADMIN = "STARTER_ADMIN";
    public static final String STARTER_READ = "STARTER_READ";
    public static final String STARTER_WRITE = "STARTER_WRITE";

    private final JwtAuthConverter jwtAuthConverter;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowCredentials(true);
//        configuration.setAllowedHeaders(List.of("Uploader-cookie", "*"));
//        configuration.setExposedHeaders(List.of("Uploader-cookie", "*"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    /**
     * This is auth chain is for a keycloak token. Every request goes through this method chain.
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**");

        http.csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .exceptionHandling(failureHandler -> failureHandler.accessDeniedHandler(new CustomAccessDeniedHandler()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/api/health/**").permitAll()
                                .requestMatchers("/api/admin/**").hasRole(STARTER_ADMIN)
                                .anyRequest().authenticated()
                );

        http.oauth2ResourceServer(resourceConfig -> resourceConfig
                .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }

}
