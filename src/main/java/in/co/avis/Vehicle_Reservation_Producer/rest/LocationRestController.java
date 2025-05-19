package in.co.avis.Vehicle_Reservation_Producer.rest;

import in.co.avis.Vehicle_Reservation_Producer.entity.Location;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST controller for managing locations.
 * Handles all CRUD operations and restricts modification operations to ADMIN users only.
 */
@RestController
@RequestMapping("/locations")
public class LocationRestController {

    private final LocationService locationService;

    public LocationRestController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * GET /locations
     * Retrieves a list of all locations.
     */
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    /**
     * GET /locations/{id}
     * Retrieves a location by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable int id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    /**
     * POST /locations
     * Creates a new location. Only accessible by ADMIN users.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createLocation(
            @Valid @RequestBody Location location,
            @AuthenticationPrincipal UserInfo userInfo) {

        User user = userInfo.getUser();

        // Check if user is ADMIN
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized, You are not Admin!"));
        }

        locationService.createLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Location created successfully"));
    }

    /**
     * PUT /locations/{id}
     * Updates an existing location. Only accessible by ADMIN users.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateLocation(
            @PathVariable int id,
            @Valid @RequestBody Location locationDetails,
            @AuthenticationPrincipal UserInfo userInfo) {

        User user = userInfo.getUser();

        // Only allow admins to update
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized, You are not Admin!"));
        }

        locationService.updateLocation(id, locationDetails);
        return ResponseEntity.ok(Map.of("message", "Location updated successfully"));
    }

    /**
     * DELETE /locations/{id}
     * Deletes a location by its ID. Only accessible by ADMIN users.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLocation(
            @PathVariable int id,
            @AuthenticationPrincipal UserInfo userInfo) {

        User user = userInfo.getUser();

        // Only allow admins to delete
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized, You are not Admin!"));
        }

        locationService.deleteLocation(id);
        return ResponseEntity.ok(Map.of("message", "Location deleted successfully"));
    }
}
