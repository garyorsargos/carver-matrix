package com.fmc.starterApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fmc.starterApp.models.entity.MatrixImage;

@Repository
public interface MatrixImageRepository extends JpaRepository<MatrixImage, Long> {
}
