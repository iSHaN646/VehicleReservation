package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.dto.BookingRequestDto;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller to handle vehicle booking requests.
 * This includes rendering the booking form and submitting the booking request.
 */
@Controller
public class BookingController {

    private final BookingService bookingService;
    private final CarRepository carRepository;
    private final LocationRepository locationRepository;

    public BookingController(BookingService bookingService, CarRepository carRepository, LocationRepository locationRepository) {
        this.bookingService = bookingService;
        this.carRepository = carRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Handles the GET request for the booking form.
     * It loads all available cars and locations and prepares a new BookingRequestDto.
     *
     * @param userInfo Authenticated user information.
     * @param model    Spring model to pass data to the view.
     * @return Returns the booking page view.
     */
    @GetMapping("/createBooking")
    public String addBooking(@AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();

        // Fetch all available cars and all locations for dropdowns
        List<Car> allAvailableCars = carRepository.findByStatus(Car.CarStatus.AVAILABLE);
        List<Location> allLocations = locationRepository.findAll();

        // Populate the model with data needed by the booking form
        model.addAttribute("user", user);
        model.addAttribute("allAvailableCars", allAvailableCars);
        model.addAttribute("allLocations", allLocations);
        model.addAttribute("bookingRequestDto", new BookingRequestDto());

        return "Booking"; // Thymeleaf template name
    }

    /**
     * Handles the POST request when a user submits the booking form.
     * It delegates the booking logic to the BookingService.
     *
     * @param bookingRequestDto Booking request data submitted from the form.
     * @param redirectAttributes Used to pass flash attributes after redirect.
     * @return Redirects back to the booking page with a success message.
     */
    @PostMapping("/submitBooking")
    public String submitBooking(@ModelAttribute BookingRequestDto bookingRequestDto,
                                RedirectAttributes redirectAttributes) {

        // Perform the booking using the service
        bookingService.bookCar(bookingRequestDto);

        // Add a success message to be displayed on the redirected page
        redirectAttributes.addFlashAttribute("message", "Booking successfully submitted!");

        return "redirect:/createBooking";
    }

}
