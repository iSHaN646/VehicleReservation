package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.dto.BookingRequestDto;
import in.co.avis.Vehicle_Reservation_Producer.dto.GetBookingDto;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setUserId(user.getId());
        model.addAttribute("bookingRequestDto",bookingRequestDto);

        return "Booking"; // Thymeleaf template name
    }

    /**
     * Handles the POST request when a user submits the booking form.
     * It delegates the booking logic to the BookingService.
     */
    @PostMapping("/submitBooking")
    public String submitBooking(@Valid @ModelAttribute BookingRequestDto bookingRequestDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model,
                                @AuthenticationPrincipal UserInfo userInfo) {

        // Custom logic: source != destination
        if (bookingRequestDto.getSourceLocationId() == bookingRequestDto.getDestinationLocationId()) {
            bindingResult.rejectValue("destinationLocationId", "error.destinationLocationId", "Destination cannot be same as source.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userInfo.getUser());
            model.addAttribute("allAvailableCars", carRepository.findByStatus(Car.CarStatus.AVAILABLE));
            model.addAttribute("allLocations", locationRepository.findAll());
            model.addAttribute("bookingRequestDto", bookingRequestDto);
            return "Booking"; // Redisplay form with errors
        }

        bookingService.bookCar(bookingRequestDto);
        redirectAttributes.addFlashAttribute("message", "Booking successfully submitted!");
        return "redirect:/createBooking";
    }

    /**
     * Handles the GET request to modify an existing booking.
     * It fetches the booking by ID, verifies the user's identity, and pre-populates the booking form.
     *
     * @param bookingId ID of the booking to be modified.
     * @param userInfo  Authenticated user information.
     * @param model     Spring model to pass data to the view.
     * @return Returns the booking page pre-filled for editing, or redirects to login if unauthorized.
     */
    @GetMapping("/modifyBooking/{bookingId}")
    public String modifyBooking(@PathVariable UUID bookingId, @AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        GetBookingDto booking = bookingService.getBookingsByBookingId(bookingId,user.getId());

        // Check if the current user is the owner of the booking or an ADMIN
        if (!Objects.equals(user.getId(), booking.getUserId()) && !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        // Fetch available data for editing
        List<Car> allAvailableCars = carRepository.findByStatus(Car.CarStatus.AVAILABLE);
        List<Location> allLocations = locationRepository.findAll();

        // Populate booking DTO with existing booking data
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setUserId(booking.getUserId());
        bookingRequestDto.setCarId(booking.getCarId());
        bookingRequestDto.setSourceLocationId(booking.getSourceLocationId());
        bookingRequestDto.setDestinationLocationId(booking.getDestinationLocationId());
        bookingRequestDto.setStartDate(booking.getStartDate());
        bookingRequestDto.setEndDate(booking.getEndDate());

        // Add data to model
        model.addAttribute("bookingRequestDto", bookingRequestDto);
        model.addAttribute("isUpdate", true);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("user", user);
        model.addAttribute("allAvailableCars", allAvailableCars);
        model.addAttribute("allLocations", allLocations);

        return "Booking"; // Thymeleaf template name
    }

    /**
     * Handles the POST request to update an existing booking.
     * It validates the user and delegates update logic to the BookingService.
     *
     * @param bookingId          ID of the booking to be updated.
     * @param dto                Updated booking data.
     * @param userInfo           Authenticated user information.
     * @param redirectAttributes Redirect attributes to pass messages after redirection.
     * @return Redirects to home page on success, or login if unauthorized.
     */
    @PostMapping("/updateBooking/{bookingId}")
    public String updateBooking(@PathVariable UUID bookingId,
                                @ModelAttribute BookingRequestDto dto,
                                @AuthenticationPrincipal UserInfo userInfo,
                                RedirectAttributes redirectAttributes) {
        User user = userInfo.getUser();

        // Validate the user is allowed to update the booking
        if (!Objects.equals(user.getId(), dto.getUserId()) && !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        // Perform the update via service layer
        bookingService.updateBooking(bookingId, dto);
        redirectAttributes.addFlashAttribute("updateSuccess", true);
        return "redirect:/";
    }

    /**
     * Handles the request to delete a booking by its ID.
     * Sends a delete event to Kafka through BookingService.
     *
     * @param bookingId UUID of the booking to delete
     * @param userInfo  Authenticated user
     * @param redirectAttributes Flash attributes for UI message
     * @return Redirects to home page or login if unauthorized
     */
    @GetMapping("/deleteBooking/{bookingId}")
    public String deleteBooking(@PathVariable UUID bookingId,
                                @AuthenticationPrincipal UserInfo userInfo,
                                RedirectAttributes redirectAttributes) {
        User user = userInfo.getUser();
        GetBookingDto booking = bookingService.getBookingsByBookingId(bookingId,user.getId());

        // Only the booking owner or an admin can delete
        if (!Objects.equals(user.getId(), booking.getUserId()) && !user.getRole().equals("ADMIN")) {
            return "redirect:/login";
        }

        bookingService.deleteBooking(bookingId);
        redirectAttributes.addFlashAttribute("message", "Booking deleted successfully.");
        return "redirect:/";
    }

}
