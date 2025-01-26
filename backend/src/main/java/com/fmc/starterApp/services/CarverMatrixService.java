package com.fmc.starterApp.services;

import com.fmc.starterApp.models.CarverMatrix;
import com.fmc.starterApp.models.User2;
import com.fmc.starterApp.repositories.CarverMatrixRepository;
import com.fmc.starterApp.repositories.User2Repository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CarverMatrixService {

    @Autowired
    private CarverMatrixRepository carverMatrixRepository;
    @Autowired
    private User2Repository user2Repository;

    //Method to get matrices where the user is a host
    public List<CarverMatrix> getMatricesByHost(Long userId) {
        return carverMatrixRepository.findByHost(userId);
    }

    //Method to get matrices where the user is a participant
    public List<CarverMatrix> getMatricesByParticipant(Long userId) {
        return carverMatrixRepository.findByParticipant(userId);
    }

    //Method to create a Carver Matrix
    public CarverMatrix createCarverMatrix(CarverMatrix matrix, Long userId) {
        //Find the user
        User2 user = user2Repository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        //Assign the user to the matrix
        matrix.setUser(user);

        //Save the matrix
        return carverMatrixRepository.save(matrix);
    }

    public CarverMatrix updateCarverMatrix(Long matrixId, CarverMatrix updatedMatrix) {
        // Find the existing matrix
        CarverMatrix existingMatrix = carverMatrixRepository.findById(matrixId).orElseThrow(() -> new IllegalArgumentException("CarverMatrix not found with ID: " + matrixId));

        // Update fields only if they are not null in the request
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

        // Save the updated matrix
        return carverMatrixRepository.save(existingMatrix);
    }
}
