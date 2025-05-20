package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.entity.Car;
import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.service.CarService;
import in.co.avis.Vehicle_Reservation_Producer.service.UserService;
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
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

//    @GetMapping("/allUsers")
//    public String showUsers(@AuthenticationPrincipal UserInfo userInfo,Model model) {
//        User user = userInfo.getUser();
//        if(!Objects.equals(user.getRole(), "ADMIN")){
//            return "redirect:/login";
//        }
//        List<User> users = userService.getAllUsers();
//        model.addAttribute("users", users);
//        return "UserList";
//    }

    @GetMapping("/allUsers")
    public String showUsers(@AuthenticationPrincipal UserInfo userInfo,
                           @RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "2") int size,
                           Model model) {
        User user = userInfo.getUser();
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.searchUsers(keyword, pageable);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "userList";
    }

    @GetMapping("/removeUser/{id}")
    public String removeUser(@PathVariable int id, @AuthenticationPrincipal UserInfo userInfo, Model model){
        User user = userInfo.getUser();
        if(!Objects.equals(user.getRole(), "ADMIN")){
            return "redirect:/login";
        }
        userService.deleteUserById(id);
        return "redirect:/allUsers";
    }

}
