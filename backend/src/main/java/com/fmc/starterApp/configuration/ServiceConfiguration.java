package com.fmc.starterApp.configuration;

import com.fmc.starterApp.repositories.PostgresRepository;
import com.fmc.starterApp.repositories.UserLogsRepository;
import com.fmc.starterApp.repositories.UsersRepository;
import com.fmc.starterApp.services.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({})
public class ServiceConfiguration {
    @Bean
    AdminService adminService(final UsersRepository usersRepository, final UserLogsRepository userLogsRepository) {
        return new AdminService(usersRepository, userLogsRepository); }

    @Bean
    PostGresExampleService postGresExampleService(final PostgresRepository postgresRepository) {
        return new PostGresExampleService(postgresRepository);
    }


}
