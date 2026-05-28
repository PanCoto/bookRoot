package pl.studyshare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.studyshare.domain.Task;
import pl.studyshare.domain.User;
import pl.studyshare.dto.AnswerCreateRequest;
import pl.studyshare.dto.TaskCreateRequest;
import pl.studyshare.dto.TaskDTO;
import pl.studyshare.dto.TaskUpdateRequest;
import pl.studyshare.enums.Role;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.repository.UserRepository;
import pl.studyshare.service.AnswerService;
import pl.studyshare.service.CategoryService;
import pl.studyshare.service.TaskService;

import java.time.LocalDate;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final CategoryService categoryService;
    private final AnswerService answerService;
    private final UserRepository userRepository;

    @GetMapping
    public String listTasks(@RequestParam(required = false) String sortField,
                            @RequestParam(required = false) String sortDir,
                            @RequestParam(required = false) LocalDate since,
                            @RequestParam(required = false) Long categoryId,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        if (sortField == null) sortField = "createdDate";
        if (sortDir == null) sortDir = "desc";

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();
        PageRequest pageable = PageRequest.of(page, 10, sort);
        Page<TaskDTO> tasks = taskService.findByFilters(since, categoryId, pageable);

        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categoryService.findAllOrderByPopularity());
        model.addAttribute("currentSort", sortField + "," + sortDir);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        return "task-list";
    }

    @GetMapping("/{id}")
    public String taskDetail(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        Task task = taskService.findEntityById(id);

        if (task.getStatus() != TaskStatus.APPROVED) {
            if (userDetails == null) {
                return "redirect:/login";
            }
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isAuthor = task.getAuthor() != null &&
                    task.getAuthor().getLogin().equals(userDetails.getUsername());

            if (!isAdmin && !isAuthor) {
                throw new SecurityException("Brak uprawnień do przeglądania tego szkicu zadania.");
            }
        }

        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("answers", answerService.findByTaskId(id));
        model.addAttribute("newAnswer", new AnswerCreateRequest("", true));
        return "task-detail";
    }

    @GetMapping("/new")
    public String newTaskForm(Model model) {
        model.addAttribute("taskCreateRequest", new TaskCreateRequest("", "", null, null, true, null));
        model.addAttribute("categories", categoryService.findAllOrderByPopularity());
        return "task-form";
    }

    @PostMapping("/new")
    public String createTask(@Valid @ModelAttribute("taskCreateRequest") TaskCreateRequest request,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllOrderByPopularity());
            return "task-form";
        }
        taskService.createTask(request, userDetails.getUsername());
        return "redirect:/tasks";
    }

    @GetMapping("/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Zalogowany użytkownik nie istnieje"));
        Task task = taskService.findEntityById(id);

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isAuthorAndDraft = task.getAuthor() != null &&
                task.getAuthor().getLogin().equals(currentUser.getLogin()) &&
                task.getStatus() == TaskStatus.DRAFT;

        if (!isAdmin && !isAuthorAndDraft) {
            throw new SecurityException("Nie możesz edytować tego zadania");
        }

        TaskUpdateRequest updateRequest = new TaskUpdateRequest(
                task.getTitle(),
                task.getContent(),
                task.getImageUrl(),
                task.getStatus(),
                task.getCategory() != null ? task.getCategory().getId() : null
        );

        model.addAttribute("taskUpdateRequest", updateRequest);
        model.addAttribute("taskId", id);
        model.addAttribute("categories", categoryService.findAllOrderByPopularity());


        return "task-edit-form";
    }

    @PostMapping("/{id}/edit")
    public String updateTask(@PathVariable Long id,
                             @Valid @ModelAttribute("taskUpdateRequest") TaskUpdateRequest request,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("taskId", id);
            model.addAttribute("categories", categoryService.findAllOrderByPopularity());
            return "task-edit-form";
        }
        User currentUser = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Nieznany użytkownik"));

        taskService.updateTask(id, request, currentUser);
        return "redirect:/tasks/" + id;
    }
}