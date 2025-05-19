package in.co.avis.Vehicle_Reservation_Producer.rest;

import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller to handle CRUD operations for User entity.
 */
@RestController
@RequestMapping("/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /users
     * Returns list of all users.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /users/{id}
     * Returns user details for the given ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * POST /users
     * Creates a new user. Validates the input.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody User user) {
        userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User created successfully"));
    }

    /**
     * PUT /users/{id}
     * Updates an existing user by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateUser(@PathVariable int id, @Valid @RequestBody User userDetails) {
        userService.updateUser(id, userDetails);
        return ResponseEntity.ok(Map.of("message", "User updated successfully"));
    }

    /**
     * DELETE /users/{id}
     * Deletes a user by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable int id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
