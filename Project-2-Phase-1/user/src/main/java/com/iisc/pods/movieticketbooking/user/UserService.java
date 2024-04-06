package com.iisc.pods.movieticketbooking.user;

import com.iisc.pods.movieticketbooking.user.exceptions.BadRequestException;
import com.iisc.pods.movieticketbooking.user.exceptions.UserNotExistException;
import com.iisc.pods.movieticketbooking.user.rest.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user related operations.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestService restService;

    /**
     * Create a new user.
     *
     * @param user User object to be created.
     * @return Created user object.
     * @throws BadRequestException If there is an issue serving the request.
     */
    @Transactional(isolation= Isolation.SERIALIZABLE)
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
    @Transactional(readOnly = true)
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
    @Transactional(isolation= Isolation.SERIALIZABLE)
    public void deleteById(Integer id) {
        if (this.getById(id) != null) {
            deleteAllRecordsForUser(id);
        } else
            throw new UserNotExistException("User with id " + id + " does not exist");
    }

    /**
     * Delete all users and related records.
     */
    public void deleteAll() {
        if (restService.deleteBookings() && restService.deleteWallet())
            userRepository.deleteAll();
        else throw new BadRequestException("Issue serving request. Please check message for more details.");
    }

    /**
     * Delete user by id and all related records.
     *
     * @param userId ID of the user.
     */
    private void deleteAllRecordsForUser(Integer userId) {
        if (restService.deleteBookings(userId) && restService.deleteWallet(userId))
            userRepository.deleteById(userId);
    }
}

