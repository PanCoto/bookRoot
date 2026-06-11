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
import pl.studyshare.service.*;
import pl.studyshare.dto.*;
import pl.studyshare.enums.SortCriteria;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;

import java.time.LocalDate;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final CategoryService categoryService;
    private final AnswerService answerService;
    private final UserRepository userRepository;
    private final VoteService voteService;
    private final ShareService shareService;
    private final CommentService commentService;
    private final CookieService cookieService;

    @GetMapping
    public String listTasks(@RequestParam(required = false) String sortField,
                            @RequestParam(required = false) String sortDir,
                            @RequestParam(required = false) LocalDate since,
                            @RequestParam(required = false) Long categoryId,
                            @RequestParam(defaultValue = "0") int page,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            HttpSession session,
                            Model model) {
        
        SortPreferences finalPrefs;

        if (sortField != null || sortDir != null) {
            SortCriteria sortBy = SortCriteria.CREATED_AT;
            if ("title".equalsIgnoreCase(sortField)) {
                sortBy = SortCriteria.TITLE;
            } else if ("popularity".equalsIgnoreCase(sortField)) {
                sortBy = SortCriteria.POPULARITY;
            }

            Sort.Direction dir = Sort.Direction.DESC;
            if ("asc".equalsIgnoreCase(sortDir)) {
                dir = Sort.Direction.ASC;
            }

            finalPrefs = new SortPreferences(sortBy, dir);
            session.setAttribute("sortPrefs", finalPrefs);
            cookieService.writeSortPreferences(response, finalPrefs);
        } else {
            Optional<SortPreferences> cookiePrefs = cookieService.readSortPreferences(request);
            if (cookiePrefs.isPresent()) {
                finalPrefs = cookiePrefs.get();
                session.setAttribute("sortPrefs", finalPrefs);
            } else {
                SortPreferences sessionPrefs = (SortPreferences) session.getAttribute("sortPrefs");
                if (sessionPrefs != null) {
                    finalPrefs = sessionPrefs;
                } else {
                    finalPrefs = SortPreferences.defaultPreferences();
                }
            }
        }

        String currentSortField = "createdDate";
        if (finalPrefs.sortBy() == SortCriteria.TITLE) {
            currentSortField = "title";
        } else if (finalPrefs.sortBy() == SortCriteria.POPULARITY) {
            currentSortField = "popularity";
        }

        String currentSortDir = finalPrefs.sortDir() == Sort.Direction.ASC ? "asc" : "desc";

        Sort sort = Sort.by(finalPrefs.sortDir(), currentSortField);
        PageRequest pageable = PageRequest.of(page, 10, sort);
        Page<TaskDTO> tasks = taskService.findByFilters(since, categoryId, pageable);

        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categoryService.findAllOrderByPopularity());
        model.addAttribute("currentSort", currentSortField + "," + currentSortDir);
        model.addAttribute("sortField", currentSortField);
        model.addAttribute("sortDir", currentSortDir);

        return "task-list";
    }

    @GetMapping("/{id}")
    public String taskDetail(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             HttpSession session,
                             Model model) {
        Task task = taskService.findEntityById(id);
        String username = (userDetails != null) ? userDetails.getUsername() : null;
        boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isAuthor = userDetails != null && task.getAuthor() != null &&
                task.getAuthor().getLogin().equals(username);

        if (task.getStatus() != TaskStatus.APPROVED) {
            if (userDetails == null) {
                return "redirect:/login";
            }
            if (!isAdmin && !isAuthor) {
                throw new SecurityException("Brak uprawnień do przeglądania tego szkicu zadania.");
            }
        }

        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("isAuthor", isAuthor);
        
        List<AnswerDTO> rawAnswers = answerService.findByTaskId(id);
        List<AnswerDTO> adjustedAnswers = voteService.getAdjustedAnswers(rawAnswers, username, session);
        Map<Long, String> userVotes = voteService.getUserVotesMap(rawAnswers, username, session);

        Map<Long, List<CommentDTO>> commentsMap = new HashMap<>();
        for (AnswerDTO answer : rawAnswers) {
            commentsMap.put(answer.id(), commentService.findCommentsByAnswerId(answer.id()));
        }

        List<ShareTokenDTO> taskShares = new ArrayList<>();
        if (username != null && (isAuthor || isAdmin)) {
            taskShares = shareService.findActiveSharesByTaskId(id, username);
        }
        
        model.addAttribute("answers", adjustedAnswers);
        model.addAttribute("userVotes", userVotes);
        model.addAttribute("commentsMap", commentsMap);
        model.addAttribute("taskShares", taskShares);
        model.addAttribute("newAnswer", new AnswerCreateRequest("", true));
        return "task-detail";
    }

    @PostMapping("/{id}/share")
    public String generateShareToken(@PathVariable Long id,
                                     @RequestParam String shareType,
                                     @RequestParam(required = false) String recipientLogin,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Long recipientUserId = null;
        if ("SPECIFIC".equals(shareType) && recipientLogin != null && !recipientLogin.trim().isEmpty()) {
            User recipient = userRepository.findByLogin(recipientLogin.trim()).orElse(null);
            if (recipient != null) {
                recipientUserId = recipient.getId();
            } else {
                return "redirect:/tasks/" + id + "?error=user_not_found";
            }
        }

        ShareCreateRequest request = new ShareCreateRequest(id, recipientUserId);
        shareService.createShareToken(request, userDetails.getUsername());

        return "redirect:/tasks/" + id;
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

    @PostMapping("/{taskId}/answers")
    public String addAnswer(@PathVariable Long taskId,
                            @Valid @ModelAttribute("newAnswer") AnswerCreateRequest request,
                            BindingResult bindingResult,
                            @AuthenticationPrincipal UserDetails userDetails,
                            HttpSession session,
                            Model model) {
        if (bindingResult.hasErrors()) {
            String username = (userDetails != null) ? userDetails.getUsername() : null;
            Task task = taskService.findEntityById(taskId);
            boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isAuthor = userDetails != null && task.getAuthor() != null &&
                    task.getAuthor().getLogin().equals(username);

            model.addAttribute("task", taskService.findById(taskId));
            model.addAttribute("isAuthor", isAuthor);
            
            List<AnswerDTO> rawAnswers = answerService.findByTaskId(taskId);
            List<AnswerDTO> adjustedAnswers = voteService.getAdjustedAnswers(rawAnswers, username, session);
            Map<Long, String> userVotes = voteService.getUserVotesMap(rawAnswers, username, session);
            
            Map<Long, List<CommentDTO>> commentsMap = new HashMap<>();
            for (AnswerDTO answer : rawAnswers) {
                commentsMap.put(answer.id(), commentService.findCommentsByAnswerId(answer.id()));
            }
            
            List<ShareTokenDTO> taskShares = new ArrayList<>();
            if (username != null && (isAuthor || isAdmin)) {
                taskShares = shareService.findActiveSharesByTaskId(taskId, username);
            }
            
            model.addAttribute("answers", adjustedAnswers);
            model.addAttribute("userVotes", userVotes);
            model.addAttribute("commentsMap", commentsMap);
            model.addAttribute("taskShares", taskShares);
            return "task-detail";
        }
        answerService.saveAnswer(taskId, request, userDetails.getUsername());
        return "redirect:/tasks/" + taskId + "#answers";
    }
}