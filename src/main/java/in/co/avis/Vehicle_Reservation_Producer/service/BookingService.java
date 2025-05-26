package in.co.avis.Vehicle_Reservation_Producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.co.avis.Vehicle_Reservation_Producer.dto.BookingRequestDto;
import in.co.avis.Vehicle_Reservation_Producer.dto.GetBookingDto;
import in.co.avis.avro.Booking;
import in.co.avis.Vehicle_Reservation_Producer.util.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * Deletes a booking by sending a delete event to Kafka.
     *
     * @param bookingId UUID of the booking to delete
     */
    public void deleteBooking(UUID bookingId) {
        try {
            // Build booking Avro object with event type DELETED
            Booking booking = Booking.newBuilder()
                    .setBookingId(bookingId.toString())
                    .setTimestamp(Instant.now().toString())
                    .setEncryptedPayload("") // No payload required for delete
                    .setEventType("DELETED")
                    .build();

            // Publish delete booking event to Kafka
            kafkaTemplate.send(KAFKA_TOPIC, bookingId.toString(), booking);

            logger.info("Booking ID: {} delete event sent to Kafka", bookingId);
        } catch (Exception e) {
            logger.error("Failed to delete booking with ID: {}", bookingId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete booking with ID: " + bookingId, e);
        }
    }

    /**
     * Fetches all bookings for a given user ID by calling the external consumer service.
     *
     * @param userId ID of the user whose bookings need to be retrieved
     * @return List of bookings belonging to the user, or an empty list if none found or in case of error
     */
    public List<GetBookingDto> getBookingsByUserId(int userId) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        RestTemplate restTemplate = new RestTemplate(factory);

        try {
            logger.info("Fetching bookings for user ID: {}", userId);
            ResponseEntity<GetBookingDto[]> response = restTemplate.getForEntity(
                    "https://vehiclereservation-consumer.onrender.com/recentBookings", GetBookingDto[].class);

            GetBookingDto[] allBookings = response.getBody();
            if (allBookings == null) {
                logger.warn("No bookings found for user ID: {}", userId);
                return List.of();
            }

            List<GetBookingDto> userBookings = Arrays.stream(allBookings)
                    .filter(b -> b.getUserId() == userId)
                    .collect(Collectors.toList());

            logger.info("Found {} bookings for user ID: {}", userBookings.size(), userId);
            return userBookings;

        } catch (Exception e) {
            logger.error("Error fetching bookings for user ID {}: {}", userId, e.getMessage(), e);
            return List.of(); //  return empty list on error
        }
    }

    /**
     * Fetches all bookings from the external consumer service.
     *
     * @return List of all bookings, or empty list in case of error
     */
    public List<GetBookingDto> getAllBookings() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        RestTemplate restTemplate = new RestTemplate(factory);

        try {
            logger.info("Fetching all bookings from external consumer service");
            ResponseEntity<GetBookingDto[]> response = restTemplate.getForEntity(
                    "https://vehiclereservation-consumer.onrender.com/recentBookings", GetBookingDto[].class);

            GetBookingDto[] allBookings = response.getBody();
            if (allBookings == null) {
                logger.warn("No bookings retrieved from external service");
                return List.of();
            }

            logger.info("Retrieved {} bookings from external service", allBookings.length);
            return Arrays.asList(allBookings);

        } catch (Exception e) {
            logger.error("Error fetching all bookings: {}", e.getMessage(), e);
            return List.of(); //  return empty list on error
        }
    }

    /**
     * Fetches a booking by its ID from the external consumer service.
     *
     * @param bookingId UUID of the booking to be retrieved
     * @return Booking DTO if found, or a new empty DTO if not found or error occurs
     */
    public GetBookingDto getBookingsByBookingId(UUID bookingId) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        RestTemplate restTemplate = new RestTemplate(factory);

        try {
            logger.info("Fetching booking with ID: {}", bookingId);
            ResponseEntity<GetBookingDto[]> response = restTemplate.getForEntity(
                    "https://vehiclereservation-consumer.onrender.com/recentBookings", GetBookingDto[].class);

            GetBookingDto[] allBookings = response.getBody();
            if (allBookings == null) {
                logger.warn("No bookings received from external service");
                return new GetBookingDto();
            }

            Optional<GetBookingDto> bookingOpt = Arrays.stream(allBookings)
                    .filter(b -> bookingId.equals(b.getBookingId()))
                    .findFirst();

            if (bookingOpt.isPresent()) {
                logger.info("Booking found for ID: {}", bookingId);
            } else {
                logger.warn("No booking found for ID: {}", bookingId);
            }

            return bookingOpt.orElse(new GetBookingDto());

        } catch (Exception e) {
            logger.error("Error fetching booking with ID {}: {}", bookingId, e.getMessage(), e);
            return new GetBookingDto(); //  return empty DTO on error
        }
    }



}
