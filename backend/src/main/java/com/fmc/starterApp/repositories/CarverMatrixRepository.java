package com.fmc.starterApp.repositories;

import com.fmc.starterApp.models.CarverMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.fmc.starterApp.models.CarverMatrix;
import com.fmc.starterApp.models.User2;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.User2Repository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface CarverMatrixRepository extends JpaRepository<CarverMatrix, Long> {

    @Query(value = "SELECT * FROM carver_matrices WHERE CAST(:userId AS text) = ANY(hosts)", nativeQuery = true)
    List<CarverMatrix> findByHost(@Param("userId") Long userId);
    
    @Query(value = "SELECT * FROM carver_matrices WHERE CAST(:userId AS text) = ANY(participants)", nativeQuery = true)
    List<CarverMatrix> findByParticipant(@Param("userId") Long userId);

}
