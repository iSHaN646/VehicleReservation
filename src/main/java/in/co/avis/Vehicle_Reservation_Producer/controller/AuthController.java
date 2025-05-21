package in.co.avis.Vehicle_Reservation_Producer.controller;

import in.co.avis.Vehicle_Reservation_Producer.entity.User;
import in.co.avis.Vehicle_Reservation_Producer.entity.UserInfo;
import in.co.avis.Vehicle_Reservation_Producer.repository.UserRepository;
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
    public String Home(@AuthenticationPrincipal UserInfo userInfo, Model model) {
        User user = userInfo.getUser();
        model.addAttribute("user", user);
        return "Home";
    }
}
