package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.Location;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.repository.CarRepository;
import in.co.avis.Vehicle_Reservation_Producer.repository.LocationRepository;
import in.co.avis.Vehicle_Reservation_Producer.service.BookingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CreateBookingController {

    private final BookingService bookingService;
    private final CarRepository carRepository;
    private final LocationRepository locationRepository;

    public CreateBookingController(BookingService bookingService, CarRepository carRepository, LocationRepository locationRepository) {
        this.bookingService = bookingService;
        this.carRepository = carRepository;
        this.locationRepository = locationRepository;
    }

    @GetMapping("/createBooking")
    public String addBooking(@AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        List<Car> allAvailableCars = carRepository.findByStatus(Car.CarStatus.AVAILABLE);
        List<Location> allLocations = locationRepository.findAll();
        model.addAttribute("user",user);
        model.addAttribute("allAvailableCars",allAvailableCars);
        model.addAttribute("allLocations",allLocations);
        return "Booking";
    }
}
