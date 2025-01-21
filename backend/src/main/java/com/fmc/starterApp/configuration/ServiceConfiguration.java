package com.fmc.starterApp.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.PostgresRepository;
import com.fmc.starterApp.repositories.User2Repository;
import com.fmc.starterApp.repositories.UserLogsRepository;
import com.fmc.starterApp.repositories.UsersRepository;
import com.fmc.starterApp.services.AdminService;
import com.fmc.starterApp.services.CarverMatrixService;
import com.fmc.starterApp.services.PostGresExampleService;
import com.fmc.starterApp.services.User2Service;

@Configuration
@EnableConfigurationProperties({})
public class ServiceConfiguration {
    @Bean
    User2Service user2Service(final User2Repository user2Repository) {
        return new User2Service(user2Repository); }

    @Bean
    CarverMatrixService carverMatrixService(final CarverMatrixRepository carverMatrixRepository, final User2Repository user2Repository) {
        return new CarverMatrixService(carverMatrixRepository, user2Repository); }

    @Bean
    AdminService adminService(final UsersRepository usersRepository, final UserLogsRepository userLogsRepository) {
        return new AdminService(usersRepository, userLogsRepository); }

    @Bean
    PostGresExampleService postGresExampleService(final PostgresRepository postgresRepository) {
        return new PostGresExampleService(postgresRepository);
    }


}
