package com.fmc.carverApp.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fmc.carverApp.repositories.PostgresRepository;
import com.fmc.carverApp.repositories.UserLogsRepository;
import com.fmc.carverApp.repositories.UsersRepository;
import com.fmc.carverApp.services.AdminService;
import com.fmc.carverApp.services.PostGresExampleService;

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
