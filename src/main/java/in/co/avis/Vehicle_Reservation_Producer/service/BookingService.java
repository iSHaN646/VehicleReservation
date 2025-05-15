package in.co.avis.Vehicle_Reservation_Producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.co.avis.Vehicle_Reservation_Producer.dto.BookingRequestDto;
import in.co.avis.Vehicle_Reservation_Producer.dto.EncryptedBookingMessage;
import in.co.avis.Vehicle_Reservation_Producer.repository.CarRepository;
import in.co.avis.Vehicle_Reservation_Producer.repository.LocationRepository;
import in.co.avis.Vehicle_Reservation_Producer.repository.UserRepository;
import in.co.avis.Vehicle_Reservation_Producer.util.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.UUID;

@Service
public class BookingService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final String KAFKA_TOPIC = "booking-events";

    public BookingService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void bookCar(BookingRequestDto bookingRequestDto) throws Exception {
        // Generate Booking ID
        UUID bookingId = UUID.randomUUID();

        // Encrypt the booking details
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String bookingDetails = objectMapper.writeValueAsString(bookingRequestDto);
        SecretKey secretKey = AESUtil.getSharedSecretKey();
        String encryptedPayload = AESUtil.encrypt(bookingDetails, secretKey);

        // Create the EncryptedBookingMessage DTO
        EncryptedBookingMessage encryptedBookingMessage = new EncryptedBookingMessage();
        encryptedBookingMessage.setBookingId(bookingId);
        encryptedBookingMessage.setTimestamp(Instant.now().toString());
        encryptedBookingMessage.setEncryptedPayload(encryptedPayload);
        encryptedBookingMessage.setEventType("CREATED");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(encryptedBookingMessage);

        // Publish the message to Kafka
        kafkaTemplate.send(KAFKA_TOPIC, bookingId.toString(), json);

        // Log the booking action
        logger.info("Booking ID: {} created and sent to Kafka", bookingId);
    }

    public void updateBooking(UUID bookingId, BookingRequestDto dto) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String bookingDetails = objectMapper.writeValueAsString(dto);
        SecretKey secretKey = AESUtil.getSharedSecretKey();
        String encryptedPayload = AESUtil.encrypt(bookingDetails, secretKey);

        // Build the updated event message with eventType "UPDATED"
        EncryptedBookingMessage encryptedBookingMessage = new EncryptedBookingMessage();
        encryptedBookingMessage.setBookingId(bookingId);
        encryptedBookingMessage.setTimestamp(Instant.now().toString());
        encryptedBookingMessage.setEncryptedPayload(encryptedPayload);
        encryptedBookingMessage.setEventType("UPDATED");


        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(encryptedBookingMessage);

        kafkaTemplate.send("booking-events",  bookingId.toString(), json);

        logger.info("Booking ID: {} updated and sent to Kafka", bookingId);
    }

}

