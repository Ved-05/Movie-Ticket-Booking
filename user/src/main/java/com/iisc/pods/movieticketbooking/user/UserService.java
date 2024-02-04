package com.iisc.pods.movieticketbooking.user;

import com.iisc.pods.movieticketbooking.user.exceptions.BadRequestException;
import com.iisc.pods.movieticketbooking.user.exceptions.UserNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for user related operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Create a new user.
     *
     * @param user User object to be created.
     * @return Created user object.
     * @throws BadRequestException If there is an issue serving the request.
     */
    public User create(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new BadRequestException("Issue serving request. Please check message for more details. " + e.getMessage());
        }
    }

    /**
     * Get user by id.
     *
     * @param id ID of the user.
     * @return User object.
     * @throws UserNotExistException If user with id does not exist.
     */
    public User getById(Integer id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotExistException("User with id " + id + " does not exist")
        );
    }

    /**
     * Delete user by id.
     *
     * @param id ID of the user.
     * @throws UserNotExistException If user with id does not exist.
     */
    public void deleteById(Integer id) {
        if (this.getById(id) != null)
            userRepository.deleteById(id);
        else
            throw new UserNotExistException("User with id " + id + " does not exist");
    }

    /**
     * Delete all users.
     */
    public void deleteAll() {
        userRepository.deleteAll();
    }
}