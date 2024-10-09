package com.fmc.carverApp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fmc.carverApp.models.PostgresExampleObject;

public interface PostgresRepository extends JpaRepository<PostgresExampleObject, Long> {
    List<PostgresExampleObject> findByName(String name);

    PostgresExampleObject findFirstById(Long id);

    void deleteAllById(Long id);


}
