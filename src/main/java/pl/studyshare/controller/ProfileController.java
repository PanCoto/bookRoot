package pl.studyshare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.studyshare.domain.User;
import pl.studyshare.repository.UserRepository;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping("/edit")
    public String editProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();
        model.addAttribute("userProfile", user);
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                String firstName, String lastName, int age) {
        User user = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        userRepository.save(user);
        return "redirect:/profile/edit?success";
    }
}