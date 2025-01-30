package com.fmc.starterApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fmc.starterApp.models.entity.PostgresExampleObject;

import java.util.List;

public interface PostgresRepository extends JpaRepository<PostgresExampleObject, Long> {
    List<PostgresExampleObject> findByName(String name);

    PostgresExampleObject findFirstById(Long id);

    void deleteAllById(Long id);


}
