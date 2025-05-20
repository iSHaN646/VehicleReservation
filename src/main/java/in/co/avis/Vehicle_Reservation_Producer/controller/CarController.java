package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.service.CarService;
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
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/allCars")
    public String showCars(@AuthenticationPrincipal UserInfo userInfo,
                           @RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "2") int size,
                           Model model) {
        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Car> carPage = carService.searchCars(keyword, pageable);

        model.addAttribute("cars", carPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", carPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "carList";
    }



    @GetMapping("/addCar")
    public String CarForm(@AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }
        Car car = new Car();
        car.setStatus(Car.CarStatus.AVAILABLE);
        model.addAttribute("car",car);
        return "CarForm";
    }

    @PostMapping("/createCar")
    public String saveCar(@Valid @ModelAttribute("car") Car car, BindingResult result) {
        if (result.hasErrors()) {
            return "CarForm";
        }
        carService.addCar(car);
        return "redirect:/allCars";
    }

    @PostMapping("/updateCar")
    public String updateCar(@Valid @ModelAttribute("car") Car car, BindingResult result, @AuthenticationPrincipal UserInfo userInfo) {
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            return "CarForm";
        }
        carService.updateCar(car);
        return "redirect:/allCars";
    }

    @GetMapping("/modifyCar/{id}")
    public String modifyCar(@PathVariable int id, @AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }
        Car car = carService.getCarById(id);
        model.addAttribute("car",car);
        return "CarForm";
    }

    @GetMapping("/removeCar/{id}")
    public String removeCar(@PathVariable int id, @AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }
        carService.deleteCar(id);
        return "redirect:/allCars";
    }

}
