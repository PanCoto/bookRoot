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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.studyshare.dto.*;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.service.AnswerService;
import pl.studyshare.service.CategoryService;
import pl.studyshare.service.TaskService;
import pl.studyshare.repository.UserRepository;
import pl.studyshare.domain.User;

import java.time.LocalDate;
import java.util.Optional;

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
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        PageRequest pageable = PageRequest.of(page, 10, sort);
        Page<TaskDTO> tasks = taskService.findByFilters(since, categoryId, pageable);
        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categoryService.findAllOrderByPopularity());
        model.addAttribute("currentSort", sortField + "," + sortDir);
        return "task-list";
    }

    @GetMapping("/{id}")
    public String taskDetail(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {

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
        User currentUser = userRepository.findByLogin(userDetails.getUsername()).orElseThrow();
        return "task-form";
    }
}