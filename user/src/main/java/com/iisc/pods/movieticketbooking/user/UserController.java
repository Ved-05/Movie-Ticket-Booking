package com.iisc.pods.movieticketbooking.user;

import com.iisc.pods.movieticketbooking.user.exceptions.BadRequestException;
import com.iisc.pods.movieticketbooking.user.exceptions.UserNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user related operations.
 */
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Integer id) {
        ResponseEntity<User> responseEntity;
        try {
            User user = userService.getById(id);
            responseEntity = new ResponseEntity<>(user, HttpStatus.OK);
        } catch (UserNotExistException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        ResponseEntity<User> responseEntity;
        try {
            User savedUser = userService.create(user);
            responseEntity = new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (BadRequestException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable Integer id) {
        ResponseEntity<User> responseEntity;
        try {
            userService.deleteById(id);
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (UserNotExistException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    @DeleteMapping("/users")
    public ResponseEntity<User> delete() {
        ResponseEntity<User> responseEntity;
        try {
            userService.deleteAll();
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }
}
