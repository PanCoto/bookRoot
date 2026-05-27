package pl.studyshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping("/edit")
    public String editProfile() {
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile() {
        return "redirect:/profile/edit?success";
    }
}