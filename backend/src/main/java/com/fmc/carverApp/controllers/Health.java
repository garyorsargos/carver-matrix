package com.fmc.carverApp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping({"/api/health"})
public class Health {
    @GetMapping()
    public ResponseEntity<String> healthcheck() {

        return new ResponseEntity<>("app is healthy", HttpStatus.OK);
    }
}
