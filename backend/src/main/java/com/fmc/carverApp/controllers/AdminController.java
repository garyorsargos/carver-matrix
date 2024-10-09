package com.fmc.carverApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fmc.carverApp.models.AppUser;
import com.fmc.carverApp.services.AdminService;

import lombok.AllArgsConstructor;


@RestController
@AllArgsConstructor
@RequestMapping({"/api/admin"})
public class AdminController {
    @Autowired
    AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<?> usersAdminData() {
        try {
            return new ResponseEntity<>(adminService.getAdminInfo(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> addKeyToRoles(@RequestBody AppUser user) {
        try{
            return new ResponseEntity<>(adminService.insertNewUser(user), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
