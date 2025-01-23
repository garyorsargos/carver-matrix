package com.fmc.starterApp.repositories;

import com.fmc.starterApp.models.MatrixImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatrixImageRepository extends JpaRepository<MatrixImage, Long> {
}
