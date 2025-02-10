package com.fmc.starterApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fmc.starterApp.models.dto.JWTInfoDTO;
import com.fmc.starterApp.models.entity.User2;
import com.fmc.starterApp.services.User2Service;

import lombok.AllArgsConstructor;


@RestController
@AllArgsConstructor
@RequestMapping({"/api/user2"})
public class User2Controller {
    @Autowired
    User2Service user2Service;

    @GetMapping("/users")
    public ResponseEntity<?> user2Data() {
        try {
            return new ResponseEntity<>(user2Service.getUserInfo(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> addUser2(@RequestBody User2 user) {
        try{
            return new ResponseEntity<>(user2Service.insertNewUser(user), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/whoami")
    public ResponseEntity<JWTInfoDTO> whoAmI(@AuthenticationPrincipal Jwt jwt) {
        JWTInfoDTO jwtInfo = user2Service.extractJwtInfo(jwt);
        return ResponseEntity.ok(jwtInfo);
    }

    // New endpoint: upsert user based on JWT information.
    @GetMapping("/whoami-upsert")
    public ResponseEntity<User2> whoAmIUpsert(@AuthenticationPrincipal Jwt jwt) {
        User2 user = user2Service.upsertUser(jwt);
        return ResponseEntity.ok(user);
    }
}
