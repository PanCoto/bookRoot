package pl.studyshare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.studyshare.domain.User;
import pl.studyshare.dto.TaskDTO;
import pl.studyshare.repository.UserRepository;
import pl.studyshare.service.TaskService;

import java.util.List;

@Controller
@RequestMapping("/moderator")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
@RequiredArgsConstructor
public class ModeratorController {

    private final UserRepository userRepository;
    private final TaskService taskService;

    @GetMapping("/tasks")
    public String moderationQueue(Model model) {
        List<TaskDTO> pendingTasks = taskService.findAllPending();
        model.addAttribute("pendingTasks", pendingTasks);
        return "moderator/tasks";
    }

    @PostMapping("/tasks/{id}/approve")
    public String approveTask(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User moderator = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono moderatora"));
        taskService.approveTask(id, moderator);
        redirectAttributes.addFlashAttribute("successMessage", "Zadanie zostało zatwierdzone.");
        return "redirect:/moderator/tasks";
    }

    @PostMapping("/tasks/{id}/reject")
    public String rejectTask(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User moderator = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono moderatora"));
        taskService.rejectTask(id, moderator);
        redirectAttributes.addFlashAttribute("successMessage", "Zadanie zostało odrzucone.");
        return "redirect:/moderator/tasks";
    }
}
