package pl.studyshare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.studyshare.domain.User;
import pl.studyshare.dto.UserUpdateRequest;
import pl.studyshare.repository.UserRepository;

/**
 * MVC controller for viewing and editing the current user's profile.
 * Implements YAML requirement: "Edycja na danych bieżących" (1p.)
 * Pre-fills form with current user data (firstName, lastName, age,
 * displayName, email, anonymousMode).
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    /**
     * GET /profile/edit – shows pre-filled profile form.
     * Edycja na danych bieżących (YAML requirement – 1p.)
     */
    @GetMapping("/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();

        // Pre-fill the form with current values (edycja na danych bieżących)
        UserUpdateRequest profile = new UserUpdateRequest(
                user.getFirstName(),
                user.getLastName(),
                user.getAge(),
                user.getDisplayName(),
                user.getEmail(),
                user.getAnonymousMode() != null ? user.getAnonymousMode() : false
        );

        model.addAttribute("userProfile", profile);
        return "profile-edit";
    }

    /**
     * POST /profile/edit – validates and saves updated profile.
     */
    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("userProfile") UserUpdateRequest request,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "profile-edit";
        }

        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setAge(request.age());
        user.setDisplayName(request.displayName());
        user.setEmail(request.email());
        user.setAnonymousMode(request.anonymousMode() != null ? request.anonymousMode() : false);

        userRepository.save(user);
        return "redirect:/profile/edit?success";
    }
}