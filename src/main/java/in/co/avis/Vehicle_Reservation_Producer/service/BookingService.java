package in.co.avis.Vehicle_Reservation_Producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.co.avis.Vehicle_Reservation_Producer.dto.BookingRequestDto;
import in.co.avis.Vehicle_Reservation_Producer.dto.EncryptedBookingMessage;
import in.co.avis.Vehicle_Reservation_Producer.repository.CarRepository;
import in.co.avis.Vehicle_Reservation_Producer.repository.LocationRepository;
import in.co.avis.Vehicle_Reservation_Producer.repository.UserRepository;
import in.co.avis.Vehicle_Reservation_Producer.util.AESUtil;
import in.co.avis.avro.Booking;
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

    private final KafkaTemplate<String, Booking> kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final String KAFKA_TOPIC = "booking-events";

    public BookingService(KafkaTemplate<String, Booking> kafkaTemplate) {
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

        Booking booking = Booking.newBuilder()
                .setBookingId(bookingId.toString())
                .setTimestamp(Instant.now().toString())
                .setEncryptedPayload(encryptedPayload)
                .setEventType("CREATED")
                .build();
        // Create the EncryptedBookingMessage DTO
//        EncryptedBookingMessage encryptedBookingMessage = new EncryptedBookingMessage();
//        encryptedBookingMessage.setBookingId(bookingId);
//        encryptedBookingMessage.setTimestamp(Instant.now().toString());
//        encryptedBookingMessage.setEncryptedPayload(encryptedPayload);
//        encryptedBookingMessage.setEventType("CREATED");

//        objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        String json = objectMapper.writeValueAsString(booking);

        // Publish the message to Kafka
        kafkaTemplate.send(KAFKA_TOPIC, bookingId.toString(), booking);

        // Log the booking action
        logger.info("Booking ID: {} created and sent to Kafka", bookingId);
    }

    public void updateBooking(UUID bookingId, BookingRequestDto dto) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String bookingDetails = objectMapper.writeValueAsString(dto);
        SecretKey secretKey = AESUtil.getSharedSecretKey();
        String encryptedPayload = AESUtil.encrypt(bookingDetails, secretKey);

        Booking booking = Booking.newBuilder()
                .setBookingId(bookingId.toString())
                .setTimestamp(Instant.now().toString())
                .setEncryptedPayload(encryptedPayload)
                .setEventType("UPDATED")
                .build();
        // Build the updated event message with eventType "UPDATED"
//        EncryptedBookingMessage encryptedBookingMessage = new EncryptedBookingMessage();
//        encryptedBookingMessage.setBookingId(bookingId);
//        encryptedBookingMessage.setTimestamp(Instant.now().toString());
//        encryptedBookingMessage.setEncryptedPayload(encryptedPayload);
//        encryptedBookingMessage.setEventType("UPDATED");


//        objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        String json = objectMapper.writeValueAsString(booking);

        kafkaTemplate.send("booking-events",  bookingId.toString(), booking);

        logger.info("Booking ID: {} updated and sent to Kafka", bookingId);
    }

}

