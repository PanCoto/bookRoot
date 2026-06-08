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
import pl.studyshare.enums.Role;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.repository.TaskRepository;
import pl.studyshare.repository.UserRepository;
import pl.studyshare.service.TaskService;
import pl.studyshare.service.UserService;

import java.util.List;

/**
 * MVC Controller for admin panel operations.
 * All endpoints require ADMIN role (enforced via Spring Security @PreAuthorize).
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final TaskService taskService;
    private final TaskRepository taskRepository;

    // ──────────────────────────────────────────────────────────────────────────
    //  Dashboard
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping
    public String dashboard(Model model) {
        long pendingCount  = taskRepository.countByStatus(TaskStatus.PENDING);
        long approvedCount = taskRepository.countByStatus(TaskStatus.APPROVED);
        long rejectedCount = taskRepository.countByStatus(TaskStatus.REJECTED);
        long userCount     = userRepository.count();

        model.addAttribute("pendingCount",  pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("userCount",     userCount);
        return "admin/dashboard";
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  User management
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        userService.deactivateUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Konto użytkownika zostało dezaktywowane.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/activate")
    public String activateUser(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + id));
        user.setEnabled(true);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Konto użytkownika zostało aktywowane.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUserActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Użytkownik nie istnieje: " + id));
        boolean wasActive = user.getEnabled() != null && user.getEnabled();
        user.setEnabled(!wasActive);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage",
                wasActive ? "Konto zostało dezaktywowane." : "Konto zostało aktywowane.");
        return "redirect:/admin/users?success";
    }

    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id,
                                 @RequestParam String newRole,
                                 RedirectAttributes redirectAttributes) {
        try {
            Role role = Role.valueOf(newRole.toUpperCase());
            userService.changeUserRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", "Rola została zmieniona na " + role + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nieprawidłowa rola: " + newRole);
        }
        return "redirect:/admin/users?success";
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Task moderation queue
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/tasks")
    public String moderationQueue(Model model) {
        List<TaskDTO> pendingTasks = taskService.findAllPending();
        model.addAttribute("pendingTasks", pendingTasks);
        return "admin/tasks";
    }

    @PostMapping("/tasks/{id}/approve")
    public String approveTask(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User admin = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono administratora"));
        taskService.approveTask(id, admin);
        redirectAttributes.addFlashAttribute("successMessage", "Zadanie zostało zatwierdzone.");
        return "redirect:/admin/tasks";
    }

    @PostMapping("/tasks/{id}/reject")
    public String rejectTask(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User admin = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono administratora"));
        taskService.rejectTask(id, admin);
        redirectAttributes.addFlashAttribute("successMessage", "Zadanie zostało odrzucone.");
        return "redirect:/admin/tasks";
    }
}