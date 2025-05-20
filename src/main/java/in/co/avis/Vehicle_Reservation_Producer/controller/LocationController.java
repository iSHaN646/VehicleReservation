package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.Location;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.service.CarService;
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

import java.util.List;
import java.util.Objects;

@Controller
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/allLocations")
    public String showLocations(@AuthenticationPrincipal UserInfo userInfo,
                           @RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "2") int size,
                           Model model) {
        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Location> LocationPage = locationService.searchLocations(keyword, pageable);

        model.addAttribute("locations", LocationPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", LocationPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "LocationList";
    }

    @GetMapping("/addLocation")
    public String LocationForm(@AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }
        Location location = new Location();
        model.addAttribute("location",location);
        return "LocationForm";
    }

    @PostMapping("/createLocation")
    public String saveLocation(@Valid @ModelAttribute("location") Location location, BindingResult result) {
        if (result.hasErrors()) {
            return "LocationForm";
        }
        locationService.createLocation(location);
        return "redirect:/allLocations";
    }

    @PostMapping("/updateLocation")
    public String updateLocation(@Valid @ModelAttribute("location") Location location, BindingResult result, @AuthenticationPrincipal UserInfo userInfo) {
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            return "LocationForm";
        }
        locationService.updateLocation(location.getId(), location);
        return "redirect:/allLocations";
    }

    @GetMapping("/modifyLocation/{id}")
    public String modifyLocation(@PathVariable int id, @AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }
        Location location = locationService.getLocationById(id);
        model.addAttribute("location",location);
        return "LocationForm";
    }

    @GetMapping("/removeLocation/{id}")
    public String removeLocation(@PathVariable int id, @AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }
        locationService.deleteLocation(id);
        return "redirect:/allLocations";
    }

}
