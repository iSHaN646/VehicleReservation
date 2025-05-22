package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.dto.BookingRequestDto;
import in.co.avis.Vehicle_Reservation_Producer.dto.BookingViewDTO;
import in.co.avis.Vehicle_Reservation_Producer.dto.GetBookingDto;
import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.Location;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.repository.UserRepository;
import in.co.avis.Vehicle_Reservation_Producer.service.BookingService;
import in.co.avis.Vehicle_Reservation_Producer.service.CarService;
import in.co.avis.Vehicle_Reservation_Producer.service.LocationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * Controller responsible for handling user authentication and registration.
 */
@Controller
public class AuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarService carService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private BookingService bookingService;

    /**
     * Displays the login page.
     *
     * @return The view name for the login page.
     */
    @GetMapping("/login")
    public String loginForm() {
        return "Login";
    }

    /**
     * Displays the signup form.
     * Adds a new empty User object to the model to bind form data.
     *
     * @param model Spring model to pass data to the view.
     * @return The view name for the signup page.
     */
    @GetMapping("/signup")
    public String showSignup(Model model) {
        model.addAttribute("user", new User());
        return "Signup";
    }

    /**
     * Processes the form submission for a new user registration.
     * Automatically sets the user's name based on the email prefix and encodes the password.
     *
     * @param user User object populated from the form.
     * @return Redirects to the login page after successful registration.
     */
    @PostMapping("/adduser")
    public String addUser(@ModelAttribute User user) {
        // Automatically extract username from email prefix
        List<String> splitList = List.of(user.getEmail().split("@"));
        user.setName(splitList.get(0));

        // Encrypt the user's password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user to the database
        userRepository.save(user);

        // Redirect to login page
        return "redirect:/login";
    }

    /**
     * Displays the home page after login.
     * Injects the authenticated user's details into the model.
     *
     * @param userInfo Authenticated user details.
     * @param model    Spring model to pass user data to the view.
     * @return The view name for the home page.
     */
    @GetMapping("/")
    public String Home(@AuthenticationPrincipal UserInfo userInfo, Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        User user = userInfo.getUser();
        System.out.println("Fetching bookings for user ID: " + user.getId());
        List<GetBookingDto> userBookings;
        if(user.getRole().equals("ADMIN")){
             userBookings = bookingService.getAllBookings();
        }else{
             userBookings = bookingService.getBookingsByUserId(user.getId());
        }
      System.out.println("Bookings fetched: " + userBookings.size());
        List<BookingViewDTO> bookingViews = userBookings.stream().map(booking -> {
            Car car = carService.getCarById(booking.getCarId());
            Location source = locationService.getLocationById(booking.getSourceLocationId());
            Location destination = locationService.getLocationById(booking.getDestinationLocationId());

            BookingViewDTO dto = new BookingViewDTO();
            dto.setUserId(booking.getUserId());
            dto.setBookingId(booking.getBookingId());
            dto.setCarName(car.getName());
            dto.setCarModel(car.getModel());
            dto.setCarImageUrl(car.getImageUrl());
            dto.setSourceLocationName(source.getName());
            dto.setDestinationLocationName(destination.getName());
            dto.setStartDate(booking.getStartDate());
            dto.setEndDate(booking.getEndDate());

            return dto;
        }).toList();

        model.addAttribute("user", user);
        model.addAttribute("cars", carService.getCarByStatus(Car.CarStatus.AVAILABLE));
        model.addAttribute("locations", locationService.getAllLocations());
        model.addAttribute("bookings", bookingViews);
        return "Home";
    }
}
