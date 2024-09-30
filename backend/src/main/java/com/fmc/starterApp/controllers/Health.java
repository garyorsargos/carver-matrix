package com.fmc.starterApp.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping({"/api/health"})
public class Health {
    @GetMapping()
    public ResponseEntity<String> healthcheck() {

        return new ResponseEntity<>("app is healthy", HttpStatus.OK);
    }
}
