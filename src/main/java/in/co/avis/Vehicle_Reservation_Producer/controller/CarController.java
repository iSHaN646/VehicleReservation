package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.service.CarService;
import in.co.avis.Vehicle_Reservation_Producer.service.ImageService;
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
 * Controller for handling car-related UI operations such as listing,
 * adding, editing, and deleting cars.
 */
@Controller
public class CarController {

    private final CarService carService;
    private final ImageService imageService;

    public CarController(CarService carService, ImageService imageService) {
        this.carService = carService;
        this.imageService = imageService;
    }

    /**
     * Displays a paginated list of cars with optional keyword-based search.
     *
     * @param userInfo The authenticated user.
     * @param keyword  Optional keyword for searching cars.
     * @param page     The page number to display.
     * @param size     The number of cars per page.
     * @param model    Model to carry data to the Thymeleaf view.
     * @return The car listing page.
     */
    @GetMapping("/allCars")
    public String showCars(@AuthenticationPrincipal UserInfo userInfo,
                           @RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "2") int size,
                           Model model) {

        User user = userInfo.getUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Car> carPage = carService.searchCars(keyword, pageable);

        model.addAttribute("cars", carPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", carPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("user", user);

        return "carList";
    }

    /**
     * Shows the form to add a new car.
     * Accessible only to ADMIN users.
     */
    @GetMapping("/addCar")
    public String CarForm(@AuthenticationPrincipal UserInfo userInfo, Model model) {
        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return "redirect:/login"; // Access denied
        }

        Car car = new Car();
        car.setStatus(Car.CarStatus.AVAILABLE); // Default status
        model.addAttribute("car", car);
        return "CarForm";
    }

    /**
     * Handles the submission of a new car form.
     *
     * @param car    The validated car object.
     * @param result Binding result for validation errors.
     * @return Redirects to all cars if successful, otherwise re-displays form.
     */
    @PostMapping("/createCar")
    public String saveCar(@Valid @ModelAttribute("car") Car car, BindingResult result,@RequestParam("file") MultipartFile file) {
        if (result.hasErrors()) {
            return "CarForm";
        }
        if (!file.isEmpty()) {
            String imageUrl = imageService.uploadFile(file);
            car.setImageUrl(imageUrl);
        }
        carService.addCar(car);
        return "redirect:/allCars";
    }

    /**
     * Updates an existing car's details.
     * Only accessible by ADMIN users.
     */
    @PostMapping("/updateCar")
    public String updateCar(@Valid @ModelAttribute("car") Car car,
                            BindingResult result,
                            @AuthenticationPrincipal UserInfo userInfo,@RequestParam("file") MultipartFile file) {

        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return "redirect:/login"; // Access denied
        }

        if (result.hasErrors()) {
            return "CarForm";
        }
        if (!file.isEmpty()) {
            String imageUrl = imageService.uploadFile(file);
            car.setImageUrl(imageUrl);
        }
        carService.updateCar(car);
        return "redirect:/allCars";
    }

    /**
     * Loads car details into the form for editing.
     * Only accessible by ADMIN users.
     *
     * @param id Car ID to be edited.
     */
    @GetMapping("/modifyCar/{id}")
    public String modifyCar(@PathVariable int id,
                            @AuthenticationPrincipal UserInfo userInfo,
                            Model model) {

        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return "redirect:/login";
        }

        Car car = carService.getCarById(id);
        model.addAttribute("car", car);
        return "CarForm";
    }

    /**
     * Deletes a car by its ID.
     * Only accessible by ADMIN users.
     *
     * @param id Car ID to delete.
     */
    @GetMapping("/removeCar/{id}")
    public String removeCar(@PathVariable int id,
                            @AuthenticationPrincipal UserInfo userInfo,
                            Model model) {

        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return "redirect:/login";
        }

        carService.deleteCar(id);
        return "redirect:/allCars";
    }
}
