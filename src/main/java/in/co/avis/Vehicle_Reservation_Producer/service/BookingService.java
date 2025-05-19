package in.co.avis.Vehicle_Reservation_Producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.co.avis.Vehicle_Reservation_Producer.dto.BookingRequestDto;
import in.co.avis.avro.Booking;
import in.co.avis.Vehicle_Reservation_Producer.util.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.UUID;

/**
 * Service class for managing car booking operations.
 * Handles booking creation and update with encrypted payload
 * and publishes booking events to Kafka.
 */
@Service
public class BookingService {

    private final KafkaTemplate<String, Booking> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final String KAFKA_TOPIC = "booking-events";

    public BookingService(KafkaTemplate<String, Booking> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Books a car by creating a new booking event.
     * Encrypts booking details and sends to Kafka.
     *
     * @param bookingRequestDto Booking details DTO
     */
    public void bookCar(BookingRequestDto bookingRequestDto) {
        try {
            // Generate unique booking ID
            UUID bookingId = UUID.randomUUID();

            // Serialize booking details to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String bookingDetails = objectMapper.writeValueAsString(bookingRequestDto);

            // Encrypt booking details
            SecretKey secretKey = AESUtil.getSharedSecretKey();
            String encryptedPayload = AESUtil.encrypt(bookingDetails, secretKey);

            // Build booking Avro object
            Booking booking = Booking.newBuilder()
                    .setBookingId(bookingId.toString())
                    .setTimestamp(Instant.now().toString())
                    .setEncryptedPayload(encryptedPayload)
                    .setEventType("CREATED")
                    .build();

            // Publish booking event to Kafka
            kafkaTemplate.send(KAFKA_TOPIC, bookingId.toString(), booking);

            logger.info("Booking ID: {} created and sent to Kafka", bookingId);
        } catch (Exception e) {
            logger.error("Failed to create booking", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create booking", e);
        }
    }

    /**
     * Updates an existing booking event.
     * Encrypts updated booking details and sends to Kafka.
     *
     * @param bookingId UUID of the booking to update
     * @param dto Updated booking details DTO
     */
    public void updateBooking(UUID bookingId, BookingRequestDto dto) {
        try {
            // Serialize updated booking details to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String bookingDetails = objectMapper.writeValueAsString(dto);

            // Encrypt booking details
            SecretKey secretKey = AESUtil.getSharedSecretKey();
            String encryptedPayload = AESUtil.encrypt(bookingDetails, secretKey);

            // Build booking Avro object with event type UPDATED
            Booking booking = Booking.newBuilder()
                    .setBookingId(bookingId.toString())
                    .setTimestamp(Instant.now().toString())
                    .setEncryptedPayload(encryptedPayload)
                    .setEventType("UPDATED")
                    .build();

            // Publish updated booking event to Kafka
            kafkaTemplate.send(KAFKA_TOPIC, bookingId.toString(), booking);

            logger.info("Booking ID: {} updated and sent to Kafka", bookingId);
        } catch (Exception e) {
            logger.error("Failed to update booking with ID: {}", bookingId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update booking with ID: " + bookingId, e);
        }
    }
}
