package in.co.avis.Vehicle_Reservation_Producer.rest;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.service.CarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/cars")
public class CarRestController {

    private final CarService carService;
    private static final Logger logger = LoggerFactory.getLogger(CarRestController.class);

    public CarRestController(CarService carService) {
        this.carService = carService;
    }

    /**
     * Creates a new car.
     * Only accessible by users with ADMIN role.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createCar(@Valid @RequestBody Car car, @AuthenticationPrincipal UserInfo userInfo) {
        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized, You are not Admin!"));
        }
        carService.addCar(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Car created successfully"));
    }

    /**
     * Retrieves all cars.
     */
    @GetMapping
    public ResponseEntity<List<Car>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    /**
     * Updates an existing car by ID.
     * Only accessible by users with ADMIN role.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateCar(@PathVariable int id, @Valid @RequestBody Car carUpdate, @AuthenticationPrincipal UserInfo userInfo) {
        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized, You are not Admin!"));
        }
        Car car = carService.getCarById(id);
        car.setName(carUpdate.getName());
        car.setModel(carUpdate.getModel());
        car.setType(carUpdate.getType());
        car.setPricePerDay(carUpdate.getPricePerDay());
        car.setStatus(carUpdate.getStatus());
        carService.updateCar(car);
        return ResponseEntity.ok(Map.of("message", "Car updated successfully"));
    }

    /**
     * Deletes a car by ID.
     * Only accessible by users with ADMIN role.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCar(@PathVariable int id, @AuthenticationPrincipal UserInfo userInfo) {
        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized, You are not Admin!"));
        }
        carService.deleteCar(id);
        return ResponseEntity.ok(Map.of("message", "Car deleted successfully"));
    }

    /**
     * Retrieves a car by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable int id) {
        Car car = carService.getCarById(id);
        return ResponseEntity.ok(car);
    }

    /**
     * Searches for cars by status.
     * Defaults to searching for AVAILABLE cars.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Car>> searchAvailableCars(
            @RequestParam(name = "status", defaultValue = "AVAILABLE") Car.CarStatus status) {
        List<Car> availableCars = carService.getCarByStatus(status);
        return ResponseEntity.ok(availableCars);
    }
}
