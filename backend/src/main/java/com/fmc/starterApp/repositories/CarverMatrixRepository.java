package com.fmc.starterApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.fmc.starterApp.models.entity.CarverMatrix;

import java.util.List;

@Repository
public interface CarverMatrixRepository extends JpaRepository<CarverMatrix, Long>, JpaSpecificationExecutor<CarverMatrix> {

    @Query(value = "SELECT * FROM carver_matrices WHERE CAST(:userId AS text) = ANY(hosts)", nativeQuery = true)
    List<CarverMatrix> findByHost(@Param("userId") String userId);

    @Query(value = "SELECT * FROM carver_matrices WHERE CAST(:userId AS text) = ANY(participants)", nativeQuery = true)
    List<CarverMatrix> findByParticipant(@Param("userId") String userId);
}