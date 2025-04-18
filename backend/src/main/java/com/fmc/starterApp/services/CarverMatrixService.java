package com.fmc.starterApp.services;

import java.io.IOException;
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
import com.fmc.starterApp.repositories.MatrixImageRepository;
import com.fmc.starterApp.repositories.User2Repository;

import lombok.AllArgsConstructor;

/**
 * Service class for managing CarverMatrix entities and associated CarverItems.
 *
 * <p>This service provides methods to:
 * <ul>
 *   <li>Retrieve matrices by host or participant user ID</li>
 *   <li>Retrieve a single matrix by its ID, ensuring its items are initialized</li>
 *   <li>Create a new matrix with proper user assignment and item initialization (including optional random assignment)</li>
 *   <li>Update an existing matrix with new values</li>
 *   <li>Search matrices based on provided search parameters</li>
 *   <li>Update CarverItems based on a list of update maps for a given user</li>
 * </ul>
 * 
 * <p>Each method contains error handling to capture repository failures and invalid input scenarios.
 */
@Service
@AllArgsConstructor
public class CarverMatrixService {

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;
    
    @Autowired
    private User2Repository user2Repository;
    
    @Autowired
    private CarverItemRepository carverItemRepository;
    @Autowired
    private ImageService imageService;
    @Autowired
    private MatrixImageRepository matrixImageRepository;

    /**
     * Retrieves all CarverMatrix objects where the specified user (by userId) is a host.
     *
     * <p>This method converts the userId to a string and queries the repository using a native query.
     * It initializes the items of each matrix to ensure that lazy-loaded collections are available.
     *
     * @param userId the ID of the user.
     * @return a list of CarverMatrix objects where the user is a host.
     * @throws IllegalArgumentException if userId is null.
     * @throws RuntimeException if the repository operation fails.
     */
    @Transactional
    public List<CarverMatrix> getMatricesByHost(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        try {
            List<CarverMatrix> matrices = carverMatrixRepository.findByHost(String.valueOf(userId));
            // Ensure that the items collection is initialized for each matrix.
            matrices.forEach(matrix -> Hibernate.initialize(matrix.getItems()));
            return matrices;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve matrices by host for userId: " + userId, e);
        }
    }

    /**
     * Retrieves all CarverMatrix objects where the specified user (by userId) is a participant.
     *
     * <p>This method converts the userId to a string and queries the repository using a native query.
     * It initializes the items of each matrix to ensure that lazy-loaded collections are available.
     *
     * @param userId the ID of the user.
     * @return a list of CarverMatrix objects where the user is a participant.
     * @throws IllegalArgumentException if userId is null.
     * @throws RuntimeException if the repository operation fails.
     */
    @Transactional
    public List<CarverMatrix> getMatricesByParticipant(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId must not be null");
        }
        try {
            List<CarverMatrix> matrices = carverMatrixRepository.findByParticipant(String.valueOf(userId));
            matrices.forEach(matrix -> Hibernate.initialize(matrix.getItems()));
            return matrices;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve matrices by participant for userId: " + userId, e);
        }
    }

    /**
     * Retrieves a CarverMatrix by its matrixId.
     *
     * <p>If the matrix is found, its items are initialized.
     *
     * @param matrixId the unique identifier of the matrix.
     * @return the CarverMatrix if found, or null if not.
     * @throws IllegalArgumentException if matrixId is null.
     * @throws RuntimeException if the repository operation fails.
     */
    @Transactional
    public CarverMatrix getMatrixById(Long matrixId) {
        if (matrixId == null) {
            throw new IllegalArgumentException("MatrixId must not be null");
        }
        try {
          CarverMatrix matrix = carverMatrixRepository.findFirstByMatrixId(matrixId);
          if (matrix != null) {
              Hibernate.initialize(matrix.getItems());

              //Fetch images for matrix
              List<Map<String, Object>> images = matrixImageRepository.findAll().stream().filter(img -> img.getCarverMatrix() != null && img.getCarverMatrix().getMatrixId().equals(matrixId)).map(img -> {
                      Map<String, Object> map = new HashMap<>();
                      map.put("imageId", img.getImageId());
                      map.put("imageUrl", img.getImageUrl());
                      map.put("itemId", img.getCarverItem() != null ? img.getCarverItem().getItemId() : null);
                      return map;
                  }).toList();
              matrix.setImages(images);
          }else{
             throw new IllegalArgumentException("CarverMatrix must not be null");
          }
          return matrix;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve CarverMatrix with ID: " + matrixId, e);
        }
    }

    /**
     * Creates a new CarverMatrix for the specified user.
     *
     * <p>This method retrieves the user by userId and assigns the user to the matrix.
     * It then applies multiplier values and boolean settings, and sets up the relationship between
     * the matrix and its items. If random assignment is enabled and participants are provided, it randomly assigns
     * participants to CarverItems. Otherwise, it initializes the items with empty targetUsers arrays and default metric maps.
     *
     * @param matrix the CarverMatrix to create; must not be null.
     * @param userId the ID of the user creating the matrix; must not be null.
     * @return the persisted CarverMatrix.
     * @throws IllegalArgumentException if matrix or userId is null, or if the user is not found.
     * @throws RuntimeException if the repository operation fails.
     */
    public CarverMatrix createCarverMatrix(CarverMatrix matrix, Long userId) {
        User2 user = user2Repository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        if(matrix==null) throw new IllegalArgumentException("CarverMatrix must not be null");

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

        List<String> participantList = new ArrayList<>(Arrays.asList(matrix.getParticipants()));
        for(int i = 0; i<participantList.size(); i++){
            participantList.set(i, participantList.get(i).toLowerCase().trim());
        }
        matrix.setParticipants(participantList.toArray(new String[0]));

        List<String> hostList = new ArrayList<>(Arrays.asList(matrix.getHosts()));
        for(int i = 0; i<hostList.size(); i++){
            hostList.set(i, hostList.get(i).toLowerCase().trim());
        }
        matrix.setHosts(hostList.toArray(new String[0]));

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

        CarverMatrix savedMatrix = carverMatrixRepository.save(matrix);

        for (CarverItem item : savedMatrix.getItems()) {
            if (item.getBase64Images() != null) {
                for (String base64String : item.getBase64Images()) {
                    try {
                        imageService.uploadBase64Image(base64String, savedMatrix.getMatrixId(), item.getItemId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return savedMatrix;
    }

    /**
     * Updates an existing CarverMatrix with new values.
     *
     * <p>This method retrieves the existing matrix by its ID and updates its fields based on the provided updatedMatrix.
     * Only non-null values in updatedMatrix are used to update the existing matrix.
     *
     * @param matrixId      the ID of the matrix to update; must not be null.
     * @param updatedMatrix the matrix object containing updated values; must not be null.
     * @return the updated CarverMatrix.
     * @throws IllegalArgumentException if matrixId or updatedMatrix is null, or if the matrix is not found.
     * @throws RuntimeException if the repository operation fails.
     */
    @Transactional
    public CarverMatrix updateCarverMatrix(Long matrixId, CarverMatrix updatedMatrix) {
        if (matrixId == null) {
            throw new IllegalArgumentException("MatrixId must not be null");
        }
        if (updatedMatrix == null) {
            throw new IllegalArgumentException("Updated matrix must not be null");
        }
        try {
            CarverMatrix existingMatrix = carverMatrixRepository.findById(matrixId)
                    .orElseThrow(() -> new IllegalArgumentException("CarverMatrix not found with ID: " + matrixId));

            // Update basic fields if new values are provided.
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

            // Update multiplier fields if provided.
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

            // Update assignment settings if provided.
            if (updatedMatrix.getRandomAssignment() != null) {
                existingMatrix.setRandomAssignment(updatedMatrix.getRandomAssignment());
            }
            if (updatedMatrix.getRoleBased() != null) {
                existingMatrix.setRoleBased(updatedMatrix.getRoleBased());
            }
            if (updatedMatrix.getFivePointScoring() != null) {
                existingMatrix.setFivePointScoring(updatedMatrix.getFivePointScoring());
            }

            // Ensure that the items collection is initialized.
            Hibernate.initialize(existingMatrix.getItems());

            // Save and return the updated matrix.
            return carverMatrixRepository.save(existingMatrix);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new RuntimeException("Failed to update CarverMatrix with ID: " + matrixId, e);
        }
    }

    /**
     * Searches for CarverMatrix objects based on provided search parameters.
     *
     * <p>This method first extracts a "userEmail" parameter from the search parameters.
     * If not provided or empty, it returns an empty list. Otherwise, it retrieves all matrices
     * where the user is either a host or participant, removes duplicates, and then applies additional filters.
     *
     * @param searchParams a map of search parameters; must not be null.
     * @return a list of CarverMatrix objects matching the search criteria.
     * @throws IllegalArgumentException if searchParams is null.
     * @throws RuntimeException if the repository operation fails.
     */
    public List<CarverMatrix> searchCarverMatrices(Map<String, String> searchParams) {
        if (searchParams == null) {
            throw new IllegalArgumentException("Search parameters must not be null");
        }
        // Extract the user's email from search parameters.
        String userEmail = searchParams.remove("userEmail");
        if (userEmail == null || userEmail.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Retrieve matrices where the user is a host or a participant.
            Set<CarverMatrix> userMatrices = new HashSet<>();
            List<CarverMatrix> hostMatrices = carverMatrixRepository.findByHost(userEmail);
            List<CarverMatrix> participantMatrices = carverMatrixRepository.findByParticipant(userEmail);
            userMatrices.addAll(hostMatrices);
            userMatrices.addAll(participantMatrices);

            // Convert the set to a list.
            List<CarverMatrix> uniqueMatrices = new ArrayList<>(userMatrices);

            // If there are no additional search parameters, return the matrices.
            if (searchParams.isEmpty()) {
                return uniqueMatrices;
            }

            // Apply additional filters (e.g., name and description).
            return uniqueMatrices.stream()
                .filter(matrix -> {
                    if (searchParams.containsKey("name") && !searchParams.get("name").isEmpty()) {
                        if (!matrix.getName().toLowerCase().contains(searchParams.get("name").toLowerCase())) {
                            return false;
                        }
                    }
                    if (searchParams.containsKey("description") && !searchParams.get("description").isEmpty()) {
                        if (matrix.getDescription() == null ||
                            !matrix.getDescription().toLowerCase().contains(searchParams.get("description").toLowerCase())) {
                            return false;
                        }
                    }
                    // Additional field filters can be implemented here.
                    return true;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search CarverMatrices with parameters: " + searchParams, e);
        }
    }

    /**
     * Updates CarverItem objects for a given CarverMatrix based on provided update maps.
     *
     * <p>This method iterates over a list of update maps, retrieves each CarverItem by its ID,
     * verifies that it belongs to the given matrix, applies updates to various metric fields based on the user's email,
     * and saves the updated items.
     *
     * @param matrix    the CarverMatrix whose items are to be updated; must not be null.
     * @param updates   a list of maps containing update data for CarverItems; must not be null.
     * @param userEmail the email of the user providing the updates; must not be null or empty.
     * @return a list of updated CarverItem objects.
     * @throws IllegalArgumentException if matrix, updates, or userEmail is null/empty, or if any CarverItem is not found or does not belong to the matrix.
     * @throws RuntimeException if the repository operation fails.
     */
    @Transactional
    public List<CarverItem> updateCarverItemsFromMap(CarverMatrix matrix, List<Map<String, Object>> updates, String userEmail) {
        if (matrix == null) {
            throw new IllegalArgumentException("CarverMatrix must not be null");
        }
        if (updates == null) {
            throw new IllegalArgumentException("Updates list must not be null");
        }
        if (userEmail == null || userEmail.isEmpty()) {
            throw new IllegalArgumentException("User email must not be null or empty");
        }
        List<CarverItem> updatedItems = new ArrayList<>();

        for (Map<String, Object> update : updates) {
            Long itemId = ((Number) update.get("itemId")).longValue();

            CarverItem item = carverItemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("CarverItem not found with ID: " + itemId));

            if (!item.getCarverMatrix().getMatrixId().equals(matrix.getMatrixId())) {
                throw new IllegalArgumentException("CarverItem " + itemId + " does not belong to matrix " + matrix.getMatrixId());
            }


            // Initialize metric maps if null.
            if (item.getCriticality() == null) item.setCriticality(new HashMap<>());
            if (item.getAccessibility() == null) item.setAccessibility(new HashMap<>());
            if (item.getRecoverability() == null) item.setRecoverability(new HashMap<>());
            if (item.getVulnerability() == null) item.setVulnerability(new HashMap<>());
            if (item.getEffect() == null) item.setEffect(new HashMap<>());
            if (item.getRecognizability() == null) item.setRecognizability(new HashMap<>());

            // Update metrics if present in the update map.
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

        // Save all updated items.
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
