package com.fmc.starterApp.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fmc.starterApp.models.entity.CarverItem;
import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.User2;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.User2Repository;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CarverMatrixService {

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;
    @Autowired
    private User2Repository user2Repository;

    @Transactional
    public List<CarverMatrix> getMatricesByHost(Long userId) {
        List<CarverMatrix> matrices = carverMatrixRepository.findByHost(String.valueOf(userId));
        matrices.forEach(matrix -> Hibernate.initialize(matrix.getItems()));
        return matrices;
    }

    @Transactional
    public List<CarverMatrix> getMatricesByParticipant(Long userId) {
        List<CarverMatrix> matrices = carverMatrixRepository.findByParticipant(String.valueOf(userId));
        matrices.forEach(matrix -> Hibernate.initialize(matrix.getItems()));
        return matrices;
    }

    @Transactional
    public CarverMatrix getMatrixById(Long matrixId) {
        CarverMatrix matrix = carverMatrixRepository.findFirstByMatrixId(matrixId);
        if (matrix != null) {
            Hibernate.initialize(matrix.getItems());
        }
        return matrix;
    }


    public CarverMatrix createCarverMatrix(CarverMatrix matrix, Long userId) {
        User2 user = user2Repository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        matrix.setUser(user);

        matrix.setCMulti(matrix.getCMulti());
        matrix.setAMulti(matrix.getAMulti());
        matrix.setRMulti(matrix.getRMulti());
        matrix.setVMulti(matrix.getVMulti());
        matrix.setEMulti(matrix.getEMulti());
        matrix.setR2Multi(matrix.getR2Multi());

        matrix.setRandomAssignment(matrix.getRandomAssignment());
        matrix.setRoleBased(matrix.getRoleBased());
        matrix.setFivePointScoring(matrix.getFivePointScoring());

        if (matrix.getItems() != null) {
            for (CarverItem item : matrix.getItems()) {
                item.setCarverMatrix(matrix);
            }
        }

        return carverMatrixRepository.save(matrix);
    }

    @Transactional
    public CarverMatrix updateCarverMatrix(Long matrixId, CarverMatrix updatedMatrix) {
        CarverMatrix existingMatrix = carverMatrixRepository.findById(matrixId)
                .orElseThrow(() -> new IllegalArgumentException("CarverMatrix not found with ID: " + matrixId));

        if (updatedMatrix.getName() != null) {
            existingMatrix.setName(updatedMatrix.getName());
        }
        if (updatedMatrix.getDescription() != null) {
            existingMatrix.setDescription(updatedMatrix.getDescription());
        }
        if (updatedMatrix.getHosts() != null) {
            existingMatrix.setHosts(updatedMatrix.getHosts());
        }
        if (updatedMatrix.getParticipants() != null) {
            existingMatrix.setParticipants(updatedMatrix.getParticipants());
        }

        if (updatedMatrix.getCMulti() != null) {
            existingMatrix.setCMulti(updatedMatrix.getCMulti());
        }
        if (updatedMatrix.getAMulti() != null) {
            existingMatrix.setAMulti(updatedMatrix.getAMulti());
        }
        if (updatedMatrix.getRMulti() != null) {
            existingMatrix.setRMulti(updatedMatrix.getRMulti());
        }
        if (updatedMatrix.getVMulti() != null) {
            existingMatrix.setVMulti(updatedMatrix.getVMulti());
        }
        if (updatedMatrix.getEMulti() != null) {
            existingMatrix.setEMulti(updatedMatrix.getEMulti());
        }
        if (updatedMatrix.getR2Multi() != null) {
            existingMatrix.setR2Multi(updatedMatrix.getR2Multi());
        }

        if (updatedMatrix.getRandomAssignment() != null) {
            existingMatrix.setRandomAssignment(updatedMatrix.getRandomAssignment());
        }
        if (updatedMatrix.getRoleBased() != null) {
            existingMatrix.setRoleBased(updatedMatrix.getRoleBased());
        }
        if (updatedMatrix.getFivePointScoring() != null) {
            existingMatrix.setFivePointScoring(updatedMatrix.getFivePointScoring());
        }

        Hibernate.initialize(existingMatrix.getItems());

        return carverMatrixRepository.save(existingMatrix);
    }

    public List<CarverMatrix> searchCarverMatrices(Map<String, String> searchParams) {
        Specification<CarverMatrix> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            searchParams.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    if ("name".equalsIgnoreCase(key) || "description".equalsIgnoreCase(key)) {
                        predicates.add(criteriaBuilder.like(root.get(key), "%" + value + "%"));
                    } else if (!"hosts".equalsIgnoreCase(key) && !"participants".equalsIgnoreCase(key)) {
                        predicates.add(criteriaBuilder.equal(root.get(key), value));
                    }
                }
            });

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        //Fetch initial results based on general search parameters
        List<CarverMatrix> results = carverMatrixRepository.findAll(spec);

        //Apply filtering for hosts only if provided
        if (searchParams.containsKey("hosts") && !searchParams.get("hosts").isEmpty()) {
            List<CarverMatrix> hostResults = carverMatrixRepository.findByHost(searchParams.get("hosts"));
            results.retainAll(hostResults); // Only keep items that match the host query
        }

        //Apply filtering for participants only if provided
        if (searchParams.containsKey("participants") && !searchParams.get("participants").isEmpty()) {
            List<CarverMatrix> participantResults = carverMatrixRepository.findByParticipant(searchParams.get("participants"));
            results.retainAll(participantResults);
        }

        return results;
    }
}
