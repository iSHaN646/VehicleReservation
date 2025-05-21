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

/**
 * Controller for managing user-related operations such as
 * listing all users and deleting users.
 * Only accessible by ADMIN users.
 */
@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays a paginated and searchable list of all users.
     * Only accessible by users with ADMIN role.
     *
     * @param userInfo Authenticated user information.
     * @param keyword  Search keyword to filter users.
     * @param page     Page number for pagination.
     * @param size     Number of users per page.
     * @param model    Spring model to pass data to the view.
     * @return Thymeleaf template for user list.
     */
    @GetMapping("/allUsers")
    public String showUsers(@AuthenticationPrincipal UserInfo userInfo,
                            @RequestParam(defaultValue = "") String keyword,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "2") int size,
                            Model model) {

        User user = userInfo.getUser();

        // Only ADMIN users are allowed to view all users
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.searchUsers(keyword, pageable);

        // Add attributes to be used by the Thymeleaf view
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("user", user);

        return "userList";
    }

    /**
     * Deletes a user by their ID.
     * Only accessible by users with ADMIN role.
     *
     * @param id       ID of the user to be deleted.
     * @param userInfo Authenticated user information.
     * @param model    Spring model (not used here but included for flexibility).
     * @return Redirects to the list of all users.
     */
    @GetMapping("/removeUser/{id}")
    public String removeUser(@PathVariable int id,
                             @AuthenticationPrincipal UserInfo userInfo,
                             Model model) {
        User user = userInfo.getUser();

        // Access restricted to ADMIN users
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return "redirect:/login";
        }

        userService.deleteUserById(id);
        return "redirect:/allUsers";
    }

}
