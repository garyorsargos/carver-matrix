package com.fmc.starterApp.controllers;

import com.fmc.starterApp.models.entity.AppUser;
import com.fmc.starterApp.services.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
