package in.co.avis.Vehicle_Reservation_Producer.service;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.exception.ResourceNotFoundException;
import in.co.avis.Vehicle_Reservation_Producer.repository.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class for managing car-related operations such as
 * adding, updating, deleting, and retrieving cars.
 */
@Service
public class CarService {

    private final CarRepository carRepository;
    private static final Logger logger = LoggerFactory.getLogger(CarService.class);

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * Adds a new car with default status AVAILABLE.
     */
    public Car addCar(Car car) {
        try {
            car.setStatus(Car.CarStatus.AVAILABLE); // Default status
            Car savedCar = carRepository.save(car);
            logger.info("Car added successfully: {}", savedCar);
            return savedCar;
        } catch (DataIntegrityViolationException e) {
            logger.error("Car data violates constraints: {}", e.getMostSpecificCause().getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Car data violates constraints: " + e.getMostSpecificCause().getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to add car", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add car", e);
        }
    }

    /**
     * Updates an existing car's details.
     */
    public Car updateCar(Car car) {
        try {
            Car updatedCar = carRepository.save(car);
            logger.info("Car updated successfully: {}", updatedCar);
            return updatedCar;
        } catch (DataIntegrityViolationException e) {
            logger.error("Car data violates constraints: {}", e.getMostSpecificCause().getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Car data violates constraints: " + e.getMostSpecificCause().getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to update car", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update car", e);
        }
    }

    /**
     * Deletes a car by its ID.
     */
    public void deleteCar(int id) {
        try {
            if (!carRepository.existsById(id)) {
                logger.warn("Attempt to delete non-existing car with ID: {}", id);
                throw new ResourceNotFoundException("Car with ID " + id + " not found.");
            }
            carRepository.deleteById(id);
            logger.info("Car deleted successfully with ID: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete car with ID {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete car", e);
        }
    }

    /**
     * Retrieves all cars.
     */
    public List<Car> getAllCars() {
        try {
            return carRepository.findAll();
        } catch (Exception e) {
            logger.error("Failed to fetch all cars", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve cars", e);
        }
    }

    /**
     * Retrieves a car by its ID.
     */
    public Car getCarById(int id) {
        try {
            return carRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Car with ID " + id + " not found."));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving car with ID {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving car", e);
        }
    }

    /**
     * Retrieves cars filtered by their status.
     */
    public List<Car> getCarByStatus(Car.CarStatus status) {
        try {
            return carRepository.findByStatus(status);
        } catch (Exception e) {
            logger.error("Failed to fetch cars by status: {}", status, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve cars by status", e);
        }
    }
}
