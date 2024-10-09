package com.fmc.carverApp.services;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.fmc.carverApp.models.PostgresExampleObject;
import com.fmc.carverApp.repositories.PostgresRepository;


public class PostGresExampleService {
    private final PostgresRepository postgresRepository;

    public PostGresExampleService(final PostgresRepository postgresRepository) {
        this.postgresRepository = postgresRepository;
    }

    public String insertExample(PostgresExampleObject postgresExampleObject) {
        return postgresRepository.save(postgresExampleObject).getName();
    }

    public PostgresExampleObject getObjectById(Long id) {
        return postgresRepository.findFirstById(id);
    }

    public List<PostgresExampleObject> getAllPostGres() {
        return postgresRepository.findAll();
    }

    @Transactional
    public Long deleteById(Long id) {
        postgresRepository.deleteAllById(id);
        return id;
    }
}
