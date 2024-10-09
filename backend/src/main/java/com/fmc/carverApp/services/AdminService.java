package com.fmc.carverApp.services;


import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.fmc.carverApp.models.AdminDTO;
import com.fmc.carverApp.models.AppUser;
import com.fmc.carverApp.models.UserLogs;
import com.fmc.carverApp.repositories.UserLogsRepository;
import com.fmc.carverApp.repositories.UsersRepository;

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
