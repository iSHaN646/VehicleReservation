package in.co.avis.Vehicle_Reservation_Producer.service;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.Location;
import in.co.avis.Vehicle_Reservation_Producer.exception.ResourceNotFoundException;
import in.co.avis.Vehicle_Reservation_Producer.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service class for managing Location operations.
 */
@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    public LocationService(LocationRepository locationRepository){
        this.locationRepository = locationRepository;
    }

    /**
     * Retrieves all locations.
     */
    public List<Location> getAllLocations(){
        try {
            return locationRepository.findAll();
        } catch (Exception e) {
            logger.error("Failed to fetch locations", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to retrieve locations", e);
        }
    }

    /**
     * Retrieves a location by ID. Throws 404 if not found.
     */
    public Location getLocationById(int id){
        try {
            return locationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Location with ID " + id + " not found."));
        } catch (ResourceNotFoundException e) {
            throw e; // Let custom exception bubble up
        } catch (Exception e) {
            logger.error("Error retrieving location with ID {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving location", e);
        }
    }

    /**
     * Creates a new location.
     */
    public Location createLocation(Location location){
        try {
            Location savedLocation = locationRepository.save(location);
            logger.info("Location added: {}", savedLocation);
            return savedLocation;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Location data violates constraints: " + e.getMostSpecificCause().getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to create location", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create location", e);
        }
    }

    /**
     * Updates an existing location by ID.
     */
    public Location updateLocation(int id, Location locationDetails){
        try {
            Location location = getLocationById(id);
            location.setName(locationDetails.getName());
            location.setAddress(locationDetails.getAddress());
            location.setCity(locationDetails.getCity());
            location.setState(locationDetails.getState());
            location.setZip(locationDetails.getZip());
            Location updated = locationRepository.save(location);
            logger.info("Location updated: {}", updated);
            return updated;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Location data violates constraints: " + e.getMostSpecificCause().getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to update location with ID {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update location", e);
        }
    }

    /**
     * Deletes a location by ID.
     */
    public void deleteLocation(int id){
        try {
            if (!locationRepository.existsById(id)) {
                throw new ResourceNotFoundException("Location with ID " + id + " not found.");
            }
            Location location = getLocationById(id); // Handles its own errors
            locationRepository.delete(location);
            logger.info("Location deleted with ID: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete location with ID {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete location", e);
        }
    }

    public Page<Location> searchLocations(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isEmpty()) {
            return locationRepository.findAll(pageable);
        }
        return locationRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrCityContainingIgnoreCaseOrStateContainingIgnoreCaseOrZipContainingIgnoreCase(keyword, keyword, keyword,keyword,keyword, pageable);
    }
}
