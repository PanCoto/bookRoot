package pl.studyshare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.studyshare.service.TaskService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TaskService taskService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tasks", taskService.findLatestApproved(10));
        return "home";
    }
}
