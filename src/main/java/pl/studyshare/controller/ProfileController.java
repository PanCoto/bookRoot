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

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping("/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();

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
