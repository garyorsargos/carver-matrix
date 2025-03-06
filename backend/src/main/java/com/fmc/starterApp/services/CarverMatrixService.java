package com.fmc.starterApp.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.fmc.starterApp.repositories.CarverItemRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CarverMatrixService {

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;
    @Autowired
    private User2Repository user2Repository;
    @Autowired
    private CarverItemRepository carverItemRepository;

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

        if (Boolean.TRUE.equals(matrix.getRandomAssignment()) && matrix.getParticipants() != null && matrix.getParticipants().length > 0) {
            List<String> participants = new ArrayList<>(Arrays.asList(matrix.getParticipants()));
            List<CarverItem> items = matrix.getItems();
            Random random = new Random();
            
            // Step 1: Randomly pick an initial participant per item
            Collections.shuffle(participants, random);
            List<String> initialAssignments = new ArrayList<>(participants.subList(0, Math.min(items.size(), participants.size())));
            
            // Step 2: Assign at least one participant to each item
            for (int i = 0; i < items.size(); i++) {
                items.get(i).setTargetUsers(new ArrayList<>(Collections.singletonList(initialAssignments.get(i % initialAssignments.size()))));
            }

            // Step 3: Distribute remaining participants evenly
            List<String> remainingParticipants = new ArrayList<>(participants);
            remainingParticipants.removeAll(initialAssignments);
            int index = 0;
            while (!remainingParticipants.isEmpty()) {
                items.get(index % items.size()).getTargetUsers().add(remainingParticipants.remove(0));
                index++;
            }
        } else {
            for (CarverItem item : matrix.getItems()) {
                item.setTargetUsers(new ArrayList<>());
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

    @Transactional
    public List<CarverItem> updateCarverItems(CarverMatrix matrix, List<CarverItem> itemUpdates) {
        List<CarverItem> updatedItems = new ArrayList<>();
        for (CarverItem update : itemUpdates) {
            if (update.getItemId() == null) {
                throw new IllegalArgumentException("Each update must include an itemId.");
            }
            CarverItem existingItem = carverItemRepository.findById(update.getItemId()).orElseThrow(() -> new IllegalArgumentException("CarverItem not found with ID: " + update.getItemId()));
            if (!existingItem.getCarverMatrix().getMatrixId().equals(matrix.getMatrixId())) {
                throw new IllegalArgumentException("CarverItem with ID " + update.getItemId() +" does not belong to CarverMatrix " + matrix.getMatrixId());
            }
            if (update.getCriticality() != null) {
                existingItem.setCriticality(update.getCriticality());
            }
            if (update.getAccessibility() != null) {
                existingItem.setAccessibility(update.getAccessibility());
            }
            if (update.getRecoverability() != null) {
                existingItem.setRecoverability(update.getRecoverability());
            }
            if (update.getVulnerability() != null) {
                existingItem.setVulnerability(update.getVulnerability());
            }
            if (update.getEffect() != null) {
                existingItem.setEffect(update.getEffect());
            }
            if (update.getRecognizability() != null) {
                existingItem.setRecognizability(update.getRecognizability());
            }
            updatedItems.add(existingItem);
        }
        return carverItemRepository.saveAll(updatedItems);
    }
}
