package in.co.avis.Vehicle_Reservation_Producer.rest;

import in.co.avis.Vehicle_Reservation_Producer.dto.BookingRequestDto;
import in.co.avis.Vehicle_Reservation_Producer.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for handling booking-related HTTP requests.
 */
@RestController
@RequestMapping("/bookings")
public class BookingRestController {

    private final BookingService bookingService;

    /**
     * Constructor injection of BookingService.
     */
    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Handles HTTP POST request to create a new booking.
     * Calls the booking service to process and send booking details to Kafka.
     *
     * @param bookingRequestDto DTO containing booking details from request body
     * @return ResponseEntity with success or error message
     */
    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody BookingRequestDto bookingRequestDto) {
        try {
            bookingService.bookCar(bookingRequestDto);
            return ResponseEntity.ok("Booking request processed and sent to Kafka");
        } catch (Exception e) {
            e.printStackTrace(); // Ideally use logger instead of printStackTrace
            return ResponseEntity.status(500).body("Error processing booking request");
        }
    }

    /**
     * Handles HTTP PUT request to update an existing booking.
     * Calls the booking service to update and send updated booking details to Kafka.
     *
     * @param bookingId UUID of the booking to update (from URL path)
     * @param bookingRequestDto DTO containing updated booking details from request body
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/{bookingId}")
    public ResponseEntity<String> updateBooking(@PathVariable UUID bookingId,
                                                @RequestBody BookingRequestDto bookingRequestDto) {
        try {
            bookingService.updateBooking(bookingId, bookingRequestDto);
            return ResponseEntity.ok("Booking update processed and sent to Kafka");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating booking");
        }
    }
}
