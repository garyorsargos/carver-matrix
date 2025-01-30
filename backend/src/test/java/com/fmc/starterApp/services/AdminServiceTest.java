package com.fmc.starterApp.services;

import com.fmc.starterApp.models.dto.AdminDTO;
import com.fmc.starterApp.models.entity.AppUser;
import com.fmc.starterApp.models.entity.UserLogs;
import com.fmc.starterApp.repositories.UserLogsRepository;
import com.fmc.starterApp.repositories.UsersRepository;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


class AdminServiceTest {
    UsersRepository usersRepository = mock(UsersRepository.class);
    UserLogsRepository userLogsRepository = mock(UserLogsRepository.class);
    AdminService adminService = new AdminService(usersRepository, userLogsRepository);

    @Test
    void insertNewUser_shouldPersistUser() {
        AppUser appUser = new AppUser();
        appUser.setUserName("andy_wu");
        appUser.setEmail("andy_wu@gmail.com");

        AppUser user = adminService.insertNewUser(appUser);

        verify(usersRepository, times(1)).save(any());
        verify(userLogsRepository, times(1)).save(any());
        assertEquals(user.getUserName(), "andy_wu");
        assertEquals(user.getEmail(), "andy_wu@gmail.com");
    }

    @Test
    void getAdminInfo_shouldReturnLogs() {
        Date today = new Date();
        UserLogs userLogs = new UserLogs();
        userLogs.setLoginTime(today);
        userLogs.setId(2L);

        AppUser appUser = new AppUser();
        appUser.setUserTimes(List.of(userLogs));
        appUser.setUserId(3L);
        appUser.setUserName("andy_wu");
        appUser.setEmail("andy_wu@gmail.com");

        when(usersRepository.findAll()).thenReturn(List.of(appUser));
        AdminDTO adminDTO = adminService.getAdminInfo();

        assertEquals(3L, adminDTO.getUsers().get(0).getUserId());
        assertEquals("andy_wu", adminDTO.getUsers().get(0).getUserName());
        assertEquals("andy_wu@gmail.com", adminDTO.getUsers().get(0).getEmail());
        assertEquals(today, adminDTO.getUsers().get(0).getUserTimes().get(0).getLoginTime());
    }

}