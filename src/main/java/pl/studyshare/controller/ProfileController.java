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
import pl.studyshare.dto.ChangePasswordRequest;
import pl.studyshare.repository.UserRepository;
import pl.studyshare.service.UserService;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final UserService userService;

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

    @GetMapping("/password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest("", "", ""));
        return "profile-password";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "profile-password";
        }

        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();
        try {
            userService.changePassword(user.getId(), request, userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("currentPassword", "error.changePasswordRequest", e.getMessage());
            return "profile-password";
        }

        return "redirect:/profile/password?success";
    }
}
