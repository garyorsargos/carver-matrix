package com.fmc.starterApp.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fmc.starterApp.repositories.CarverItemRepository;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.PostgresRepository;
import com.fmc.starterApp.repositories.User2Repository;
import com.fmc.starterApp.repositories.UserLogsRepository;
import com.fmc.starterApp.repositories.UsersRepository;
import com.fmc.starterApp.services.AdminService;
import com.fmc.starterApp.services.CarverMatrixService;
import com.fmc.starterApp.services.PostGresExampleService;
import com.fmc.starterApp.services.User2Service;
import com.fmc.starterApp.repositories.MatrixImageRepository;
import com.fmc.starterApp.services.ImageService;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties({})
public class ServiceConfiguration {
    @Bean
    User2Service user2Service(final User2Repository user2Repository) {
        return new User2Service(user2Repository); }

    @Bean
    CarverMatrixService carverMatrixService(final CarverMatrixRepository carverMatrixRepository, final User2Repository user2Repository, final CarverItemRepository carverItemRepository, final ImageService imageService, final MatrixImageRepository matrixImageRepository) {
        return new CarverMatrixService(carverMatrixRepository, user2Repository, carverItemRepository, imageService, matrixImageRepository); }

    @Bean
    AdminService adminService(final UsersRepository usersRepository, final UserLogsRepository userLogsRepository) {
        return new AdminService(usersRepository, userLogsRepository); }

    @Bean
    PostGresExampleService postGresExampleService(final PostgresRepository postgresRepository) {
        return new PostGresExampleService(postgresRepository);
    }

    @Bean
    ImageService imageService(final MatrixImageRepository matrixImageRepository, final CarverMatrixRepository carverMatrixRepository, final S3Client s3Client) {
        return new ImageService(matrixImageRepository, carverMatrixRepository, s3Client);
    }

}
