package in.co.avis.Vehicle_Reservation_Producer.service;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class for managing user-related operations.
 * Handles business logic and interacts with the UserRepository.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves all users from the database.
     */
    public List<User> getAllUsers(){
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Failed to fetch Users", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve users", e);
        }
    }

    /**
     * Retrieves a user by ID. Throws 404 if user not found.
     */
    public User getUserById(int id){
        try {
            return userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } catch (Exception e) {
            logger.error("Error retrieving User with ID {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching user", e);
        }
    }

    /**
     * Creates a new user with encoded password.
     * Catches and handles data integrity issues (e.g., duplicate email).
     * Logs the action
     */
    public User createUser(User user){
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser =  userRepository.save(user);
            logger.info("User created: {}", savedUser);
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User data violates constraints: " + e.getMostSpecificCause().getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to create User", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user", e);
        }
    }

    /**
     * Updates user details by ID.
     * Updates password only if new password is provided.
     * Logs the action
     */
    public User updateUser(int id, User userDetails){
        try {
            User user = getUserById(id);
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            User updatedUser = userRepository.save(user);
            logger.info("User updated: {}", updatedUser);
            return updatedUser;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User data violates constraints: " + e.getMostSpecificCause().getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to update user with ID {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update user", e);
        }
    }

//  Deletes a user by ID. Logs the action.
    public void deleteUserById(int id){
        try {
            User user = getUserById(id);
            userRepository.delete(user);
            logger.info("User deleted with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete user with ID {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete user", e);
        }
    }

    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrRoleContainingIgnoreCase(keyword, keyword, keyword, pageable);
    }
}
