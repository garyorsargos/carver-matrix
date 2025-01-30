package com.fmc.starterApp.repositories;

import com.fmc.starterApp.models.CarverMatrix;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarverMatrixRepository extends JpaRepository<CarverMatrix, Long> {

    @EntityGraph(attributePaths = {"items"})
    @Query(value = "SELECT * FROM carver_matrices WHERE CAST(:userId AS text) = ANY(hosts)", nativeQuery = true)
    List<CarverMatrix> findByHost(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"items"})
    @Query(value = "SELECT * FROM carver_matrices WHERE CAST(:userId AS text) = ANY(participants)", nativeQuery = true)
    List<CarverMatrix> findByParticipant(@Param("userId") Long userId);
}
