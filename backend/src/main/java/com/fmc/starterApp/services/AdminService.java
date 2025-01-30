package com.fmc.starterApp.services;


import com.fmc.starterApp.models.dto.AdminDTO;
import com.fmc.starterApp.models.entity.AppUser;
import com.fmc.starterApp.models.entity.UserLogs;
import com.fmc.starterApp.repositories.UserLogsRepository;
import com.fmc.starterApp.repositories.UsersRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public class AdminService {
    private final UsersRepository usersRepository;
    private final UserLogsRepository userLogsRepository;

    public AdminService(UsersRepository usersRepository, UserLogsRepository userLogsRepository) {
        this.usersRepository = usersRepository;
        this.userLogsRepository = userLogsRepository;
    }

    @Transactional
    public AppUser insertNewUser(AppUser appUser) {
        usersRepository.save(appUser);

        UserLogs userLogs = new UserLogs();
        userLogs.setAppUser(appUser);
        userLogs.setLoginTime(new Date());

        userLogsRepository.save(userLogs);
        return appUser;
    }

    public AdminDTO getAdminInfo() {
        List<AppUser> users = usersRepository.findAll();
        return AdminDTO.builder()
                .users(users)
                .totalUsers(users.size())
                .build();
    }
}
