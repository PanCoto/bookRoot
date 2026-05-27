package pl.studyshare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.studyshare.domain.User;
import pl.studyshare.dto.UserUpdateRequest;
import pl.studyshare.enums.Role;
import pl.studyshare.repository.UserRepository;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserUpdateRequest("", "", 18)); // pusty obiekt dla th:object
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid UserUpdateRequest userRequest,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               String login,
                               String password) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (userRepository.existsByLogin(login)) {
            model.addAttribute("loginExists", true);
            return "register";
        }

        User newUser = User.builder()
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .login(login)
                .password(passwordEncoder.encode(password))
                .age(userRequest.age())
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(newUser);

        redirectAttributes.addFlashAttribute("successMessage", "Rejestracja udana. Możesz się zalogować.");
        return "redirect:/login";
    }
}