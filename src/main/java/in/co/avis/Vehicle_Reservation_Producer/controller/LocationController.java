package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.Location;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.service.CarService;
import in.co.avis.Vehicle_Reservation_Producer.service.ImageService;
import in.co.avis.Vehicle_Reservation_Producer.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * Controller for managing location-related operations such as
 * viewing, adding, updating, and deleting locations.
 */
@Controller
public class LocationController {

    private final LocationService locationService;
    private final ImageService imageService;

    public LocationController(LocationService locationService, ImageService imageService) {
        this.locationService = locationService;
        this.imageService = imageService;
    }

    /**
     * Displays a paginated and searchable list of all locations.
     *
     * @param userInfo The authenticated user's info.
     * @param keyword  Optional search keyword.
     * @param page     Page number for pagination.
     * @param size     Number of items per page.
     * @param model    Spring model to pass data to the view.
     * @return Thymeleaf template for listing locations.
     */
    @GetMapping("/allLocations")
    public String showLocations(@AuthenticationPrincipal UserInfo userInfo,
                                @RequestParam(defaultValue = "") String keyword,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "2") int size,
                                Model model) {
        User user = userInfo.getUser();

        Pageable pageable = PageRequest.of(page, size);
        Page<Location> LocationPage = locationService.searchLocations(keyword, pageable);

        model.addAttribute("locations", LocationPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", LocationPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("user", user);

        return "locationList";
    }

    /**
     * Displays the form for adding a new location.
     * Only accessible by ADMIN users.
     *
     * @param userInfo The authenticated user's info.
     * @param model    Spring model to bind the form object.
     * @return Thymeleaf template for location form.
     */
    @GetMapping("/addLocation")
    public String LocationForm(@AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login"; // Access denied if not ADMIN
        }

        Location location = new Location();
        model.addAttribute("location", location);
        return "LocationForm";
    }

    /**
     * Handles the creation of a new location.
     *
     * @param location The validated location object.
     * @param result   Binding result for form validation.
     * @return Redirect to allLocations page on success.
     */
    @PostMapping("/createLocation")
    public String saveLocation(@Valid @ModelAttribute("location") Location location, BindingResult result,@RequestParam("file") MultipartFile file) {
        if (result.hasErrors()) {
            return "LocationForm"; // Validation failed
        }
        if (!file.isEmpty()) {
            String imageUrl = imageService.uploadFile(file);
            location.setImageUrl(imageUrl);
        }
        locationService.createLocation(location);
        return "redirect:/allLocations";
    }

    /**
     * Updates an existing location.
     * Only accessible by ADMIN users.
     *
     * @param location The updated location object.
     * @param result   Binding result for validation.
     * @param userInfo The authenticated user.
     * @return Redirect to allLocations on success.
     */
    @PostMapping("/updateLocation")
    public String updateLocation(@Valid @ModelAttribute("location") Location location,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserInfo userInfo,
                                 @RequestParam("file") MultipartFile file) {
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login"; // Only ADMIN allowed
        }

        if (result.hasErrors()) {
            return "LocationForm"; // Validation error
        }
        if (!file.isEmpty()) {
            String imageUrl = imageService.uploadFile(file);
            location.setImageUrl(imageUrl);
        }
        locationService.updateLocation(location.getId(), location);
        return "redirect:/allLocations";
    }

    /**
     * Loads the location object into the form for editing.
     * Only accessible by ADMIN users.
     *
     * @param id       Location ID to be edited.
     * @param userInfo The authenticated user.
     * @param model    Spring model to populate form.
     * @return Thymeleaf form page.
     */
    @GetMapping("/modifyLocation/{id}")
    public String modifyLocation(@PathVariable int id,
                                 @AuthenticationPrincipal UserInfo userInfo,
                                 Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }

        Location location = locationService.getLocationById(id);
        model.addAttribute("location", location);
        return "LocationForm";
    }

    /**
     * Deletes a location by ID.
     * Only accessible by ADMIN users.
     *
     * @param id       ID of the location to delete.
     * @param userInfo The authenticated user.
     * @param model    Spring model.
     * @return Redirect to the allLocations page.
     */
    @GetMapping("/removeLocation/{id}")
    public String removeLocation(@PathVariable int id,
                                 @AuthenticationPrincipal UserInfo userInfo,
                                 Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }

        locationService.deleteLocation(id);
        return "redirect:/allLocations";
    }
}
