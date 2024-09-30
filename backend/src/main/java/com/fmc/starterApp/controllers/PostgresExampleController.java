package com.fmc.starterApp.controllers;

import com.fmc.starterApp.models.PostgresExampleObject;
import com.fmc.starterApp.services.PostGresExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping({"/api"})
public class PostgresExampleController {
    @Autowired
    PostGresExampleService postGresExampleService;

    @PostMapping("/db")
    public ResponseEntity<String> saveDbObject(@RequestBody PostgresExampleObject test) {
        try {
            return new ResponseEntity<>(postGresExampleService.insertExample(test), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = {"/db", "/db/{id}"})
    public ResponseEntity<?> getDbObjects(@PathVariable(required = false) Optional<Long> id) {
        try {
            if(id.isPresent()) {
                return new ResponseEntity<PostgresExampleObject>(postGresExampleService.getObjectById(id.get()), HttpStatus.OK);
            } else {
                return new ResponseEntity<List<PostgresExampleObject>>(postGresExampleService.getAllPostGres(), HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/db")
    public ResponseEntity<String> updateObject(@RequestBody PostgresExampleObject test) {
        try {
            return new ResponseEntity<>(postGresExampleService.insertExample(test), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/db/{id}")
    public ResponseEntity<?> deleteObjectById(@PathVariable Long id) {
        try {
            return new ResponseEntity<>(postGresExampleService.deleteById(id), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}
