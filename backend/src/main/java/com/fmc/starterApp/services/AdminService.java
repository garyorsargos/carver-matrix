package com.fmc.starterApp.services;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.transaction.annotation.Transactional;

import com.fmc.starterApp.models.dto.AdminDTO;
import com.fmc.starterApp.models.entity.AppUser;
import com.fmc.starterApp.models.entity.UserLogs;
import com.fmc.starterApp.repositories.UserLogsRepository;
import com.fmc.starterApp.repositories.UsersRepository;

/**
 * Service class for administration-related operations.
 *
 * <p>This service is responsible for managing user data and their login logs.
 * It interacts with the {@link UsersRepository} to perform CRUD operations on
 * application users and with the {@link UserLogsRepository} to record user login events.
 *
 * <p><strong>Key Methods:</strong>
 * <ul>
 *   <li>{@link #insertNewUser(AppUser)}: Persists a new {@link AppUser} and creates a corresponding login log.</li>
 *   <li>{@link #getAdminInfo()}: Retrieves information for administration purposes, including a list of all users and the total count.</li>
 * </ul>
 */
public class AdminService {
    private final UsersRepository usersRepository;
    private final UserLogsRepository userLogsRepository;

    /**
     * Constructs an AdminService with the specified repositories.
     *
     * @param usersRepository   the repository for managing AppUser entities
     * @param userLogsRepository the repository for managing UserLogs entities
     */
    public AdminService(UsersRepository usersRepository, UserLogsRepository userLogsRepository) {
        this.usersRepository = Objects.requireNonNull(usersRepository, "usersRepository must not be null");
        this.userLogsRepository = Objects.requireNonNull(userLogsRepository, "userLogsRepository must not be null");
    }

    /**
     * Persists a new {@link AppUser} along with a corresponding login log.
     *
     * <p>This method first saves the provided {@code appUser} using the {@link UsersRepository}.
     * Then, it creates a new {@link UserLogs} entry with the current timestamp and associates it with
     * the saved user. Both operations are executed within a transaction.
     *
     * @param appUser the {@link AppUser} to persist; must not be null
     * @return the persisted {@link AppUser} entity
     * @throws IllegalArgumentException if the provided appUser is null
     * @throws RuntimeException         if any repository operation fails
     */
    @Transactional
    public AppUser insertNewUser(AppUser appUser) {
        if (appUser == null) {
            throw new IllegalArgumentException("AppUser must not be null");
        }
        try {
            // Persist the user
            usersRepository.save(appUser);

            // Create and persist the login log entry
            UserLogs userLogs = new UserLogs();
            userLogs.setAppUser(appUser);
            userLogs.setLoginTime(new Date());
            userLogsRepository.save(userLogs);

            return appUser;
        } catch (Exception e) {
            // Optionally, you can log the error here
            throw new RuntimeException("Failed to insert new user and log the login event", e);
        }
    }

    /**
     * Retrieves administrative information including the list of all users and the total user count.
     *
     * <p>This method queries the {@link UsersRepository} to retrieve all {@link AppUser} entities and builds
     * an {@link AdminDTO} with the list and the total number of users.
     *
     * @return an {@link AdminDTO} containing the list of users and the total user count
     * @throws RuntimeException if the repository query fails
     */
    public AdminDTO getAdminInfo() {
        try {
            List<AppUser> users = usersRepository.findAll();
            return AdminDTO.builder()
                    .users(users)
                    .totalUsers(users.size())
                    .build();
        } catch (Exception e) {
            // Optionally, you can log the error here
            throw new RuntimeException("Failed to retrieve administrative information", e);
        }
    }
}
