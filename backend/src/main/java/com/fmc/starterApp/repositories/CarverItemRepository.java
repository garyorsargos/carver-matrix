package com.fmc.starterApp.repositories;

import com.fmc.starterApp.models.CarverItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarverItemRepository extends JpaRepository<CarverItem, Long> {
    List<CarverItem> findByCarverMatrix_MatrixId(Long matrixId);
}

