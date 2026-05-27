package pl.studyshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
// import pl.studyshare.service.TaskService;

@Controller
public class HomeController {


    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tasks", java.util.Collections.emptyList());
        return "home";
    }
}