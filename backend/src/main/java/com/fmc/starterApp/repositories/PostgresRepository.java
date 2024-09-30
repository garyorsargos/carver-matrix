package com.fmc.starterApp.repositories;

import com.fmc.starterApp.models.PostgresExampleObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostgresRepository extends JpaRepository<PostgresExampleObject, Long> {
    List<PostgresExampleObject> findByName(String name);

    PostgresExampleObject findFirstById(Long id);

    void deleteAllById(Long id);


}
