package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.dto.BookingRequestDto;
import in.co.avis.Vehicle_Reservation_Producer.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<String> createBooking(@RequestBody BookingRequestDto bookingRequestDto) {
        try {
            bookingService.bookCar(bookingRequestDto);
            return ResponseEntity.ok("Booking request processed and sent to Kafka");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing booking request");
        }
    }

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

