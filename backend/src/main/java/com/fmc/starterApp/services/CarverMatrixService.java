package com.fmc.starterApp.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fmc.starterApp.models.entity.CarverItem;
import com.fmc.starterApp.models.entity.CarverMatrix;
import com.fmc.starterApp.models.entity.User2;
import com.fmc.starterApp.repositories.CarverItemRepository;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.User2Repository;

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

            Collections.shuffle(participants, random);
            List<String> initialAssignments = new ArrayList<>(participants.subList(0, Math.min(items.size(), participants.size())));

            for (int i = 0; i < items.size(); i++) {
                CarverItem item = items.get(i);
                String assignedUser = initialAssignments.get(i % initialAssignments.size());
                item.setTargetUsers(new String[]{assignedUser});

                if (item.getCriticality() == null) item.setCriticality(new HashMap<>());
                if (item.getAccessibility() == null) item.setAccessibility(new HashMap<>());
                if (item.getRecoverability() == null) item.setRecoverability(new HashMap<>());
                if (item.getVulnerability() == null) item.setVulnerability(new HashMap<>());
                if (item.getEffect() == null) item.setEffect(new HashMap<>());
                if (item.getRecognizability() == null) item.setRecognizability(new HashMap<>());

                item.getCriticality().put(assignedUser, 0);
                item.getAccessibility().put(assignedUser, 0);
                item.getRecoverability().put(assignedUser, 0);
                item.getVulnerability().put(assignedUser, 0);
                item.getEffect().put(assignedUser, 0);
                item.getRecognizability().put(assignedUser, 0);
            }

            List<String> remainingParticipants = new ArrayList<>(participants);
            remainingParticipants.removeAll(initialAssignments);
            Collections.shuffle(remainingParticipants, random);

            List<Integer> itemIndices = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                itemIndices.add(i);
            }
            Collections.shuffle(itemIndices, random);

            int index = 0;
            while (!remainingParticipants.isEmpty()) {
                int itemIndex = itemIndices.get(index % items.size());
                CarverItem item = items.get(itemIndex);
                String extraUser = remainingParticipants.remove(0);

                List<String> currentUsers = new ArrayList<>(Arrays.asList(item.getTargetUsers()));
                currentUsers.add(extraUser);
                item.setTargetUsers(currentUsers.toArray(new String[0]));

                if (item.getCriticality() == null) item.setCriticality(new HashMap<>());
                if (item.getAccessibility() == null) item.setAccessibility(new HashMap<>());
                if (item.getRecoverability() == null) item.setRecoverability(new HashMap<>());
                if (item.getVulnerability() == null) item.setVulnerability(new HashMap<>());
                if (item.getEffect() == null) item.setEffect(new HashMap<>());
                if (item.getRecognizability() == null) item.setRecognizability(new HashMap<>());

                item.getCriticality().put(extraUser, 0);
                item.getAccessibility().put(extraUser, 0);
                item.getRecoverability().put(extraUser, 0);
                item.getVulnerability().put(extraUser, 0);
                item.getEffect().put(extraUser, 0);
                item.getRecognizability().put(extraUser, 0);

                index++;
            }
        } else {
            for (CarverItem item : matrix.getItems()) {
                item.setTargetUsers(new String[0]);

                item.setCriticality(new HashMap<>());
                item.setAccessibility(new HashMap<>());
                item.setRecoverability(new HashMap<>());
                item.setVulnerability(new HashMap<>());
                item.setEffect(new HashMap<>());
                item.setRecognizability(new HashMap<>());
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
        // Get user's email from search params
        String userEmail = searchParams.remove("userEmail");
        if (userEmail == null || userEmail.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all matrices where user is host or participant, using a Set to prevent duplicates
        Set<CarverMatrix> userMatrices = new HashSet<>();
        List<CarverMatrix> hostMatrices = carverMatrixRepository.findByHost(userEmail);
        List<CarverMatrix> participantMatrices = carverMatrixRepository.findByParticipant(userEmail);
        userMatrices.addAll(hostMatrices);
        userMatrices.addAll(participantMatrices);

        // Convert Set back to List
        List<CarverMatrix> uniqueMatrices = new ArrayList<>(userMatrices);

        // If no other search parameters, return all user's matrices
        if (searchParams.isEmpty()) {
            return uniqueMatrices;
        }

        // Apply additional filters
        return uniqueMatrices.stream()
            .filter(matrix -> {
                // Check name filter
                if (searchParams.containsKey("name") && !searchParams.get("name").isEmpty()) {
                    if (!matrix.getName().toLowerCase().contains(searchParams.get("name").toLowerCase())) {
                        return false;
                    }
                }
                
                // Check description filter
                if (searchParams.containsKey("description") && !searchParams.get("description").isEmpty()) {
                    if (!matrix.getDescription().toLowerCase().contains(searchParams.get("description").toLowerCase())) {
                        return false;
                    }
                }

                // Check other filters
                for (Map.Entry<String, String> entry : searchParams.entrySet()) {
                    if (!"name".equalsIgnoreCase(entry.getKey()) && 
                        !"description".equalsIgnoreCase(entry.getKey()) && 
                        !"hosts".equalsIgnoreCase(entry.getKey()) && 
                        !"participants".equalsIgnoreCase(entry.getKey())) {
                        
                        String value = entry.getValue();
                        if (value != null && !value.isEmpty()) {
                            // Add any additional field checks here
                            // For now, we'll just return true for other fields
                            // as they're not critical for the search functionality
                        }
                    }
                }

                return true;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public List<CarverItem> updateCarverItemsFromMap(CarverMatrix matrix, List<Map<String, Object>> updates, String userEmail) {
        List<CarverItem> updatedItems = new ArrayList<>();

        for (Map<String, Object> update : updates) {
            Long itemId = ((Number) update.get("itemId")).longValue();

            CarverItem item = carverItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("CarverItem not found with ID: " + itemId));

            if (!item.getCarverMatrix().getMatrixId().equals(matrix.getMatrixId())) {
                throw new IllegalArgumentException("CarverItem " + itemId + " does not belong to matrix " + matrix.getMatrixId());
            }

            if (item.getCriticality() == null) item.setCriticality(new HashMap<>());
            if (item.getAccessibility() == null) item.setAccessibility(new HashMap<>());
            if (item.getRecoverability() == null) item.setRecoverability(new HashMap<>());
            if (item.getVulnerability() == null) item.setVulnerability(new HashMap<>());
            if (item.getEffect() == null) item.setEffect(new HashMap<>());
            if (item.getRecognizability() == null) item.setRecognizability(new HashMap<>());

            if (update.containsKey("criticality")) {
                item.getCriticality().put(userEmail, ((Number) update.get("criticality")).intValue());
            }
            if (update.containsKey("accessibility")) {
                item.getAccessibility().put(userEmail, ((Number) update.get("accessibility")).intValue());
            }
            if (update.containsKey("recoverability")) {
                item.getRecoverability().put(userEmail, ((Number) update.get("recoverability")).intValue());
            }
            if (update.containsKey("vulnerability")) {
                item.getVulnerability().put(userEmail, ((Number) update.get("vulnerability")).intValue());
            }
            if (update.containsKey("effect")) {
                item.getEffect().put(userEmail, ((Number) update.get("effect")).intValue());
            }
            if (update.containsKey("recognizability")) {
                item.getRecognizability().put(userEmail, ((Number) update.get("recognizability")).intValue());
            }

            updatedItems.add(item);
        }

        return carverItemRepository.saveAll(updatedItems);
    }

    /**
     * Deletes a CarverMatrix identified by its matrixId.
     * <p>
     * This method retrieves the CarverMatrix from the repository and deletes it.
     * Due to the cascading configuration (cascade = CascadeType.ALL and orphanRemoval = true),
     * any associated CarverItems will be automatically removed.
     * </p>
     *
     * @param matrixId the unique identifier of the CarverMatrix to be deleted
     * @throws IllegalArgumentException if the CarverMatrix is not found with the given matrixId
     */
    @Transactional
    public void deleteCarverMatrix(Long matrixId) {
        CarverMatrix matrix = carverMatrixRepository.findById(matrixId)
            .orElseThrow(() -> new IllegalArgumentException("CarverMatrix not found with ID: " + matrixId));
        carverMatrixRepository.delete(matrix);
    }


}
