package com.iisc.pods.movieticketbooking.user;

import com.iisc.pods.movieticketbooking.user.exceptions.BadRequestException;
import com.iisc.pods.movieticketbooking.user.exceptions.UserNotExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user related operations.
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get user by id.
     *
     * @param id user id
     * @return user details if found, else status code 404 for not found
     */
    @GetMapping("/{id}")
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

    /**
     * Create a new user.
     *
     * @param user user details
     * @return user details with status code 201 if created, else status code 400 for invalid request
     */
    @PostMapping
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

    /**
     * Deletes the user by user id.
     *
     * @param id user id
     * @return status code 200 if deleted, else status code 404 if user not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        ResponseEntity<Void> responseEntity;
        try {
            userService.deleteById(id);
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (UserNotExistException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    /**
     * Deletes all users.
     *
     * @return status code 200 if deleted, else 500
     */
    @DeleteMapping
    public ResponseEntity<Void> delete() {
        log.info("Received request to delete all users, wallets and transactions.");
        ResponseEntity<Void> responseEntity;
        try {
            userService.deleteAll();
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while deleting all users, wallets and transactions.", e);
            responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }
}
