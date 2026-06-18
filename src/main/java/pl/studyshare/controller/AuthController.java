package pl.studyshare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.studyshare.domain.User;
import pl.studyshare.dto.UserRegisterRequest;
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
        model.addAttribute("userRegisterRequest", new UserRegisterRequest("", "", "", "", "", 18, false));
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userRegisterRequest") UserRegisterRequest userRegisterRequest,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (userRepository.existsByLogin(userRegisterRequest.login())) {
            model.addAttribute("loginExists", true);
            return "register";
        }

        User newUser = User.builder()
                .firstName(userRegisterRequest.firstName())
                .lastName(userRegisterRequest.lastName())
                .login(userRegisterRequest.login())
                .email(userRegisterRequest.email())
                .password(passwordEncoder.encode(userRegisterRequest.password()))
                .age(userRegisterRequest.age())
                .role(Role.USER)
                .enabled(true)
                .build();
        userRepository.save(newUser);

        redirectAttributes.addFlashAttribute("successMessage", "Rejestracja udana. Możesz się zalogować.");
        return "redirect:/login";
    }
}
