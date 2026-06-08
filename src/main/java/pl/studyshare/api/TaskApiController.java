package pl.studyshare.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.studyshare.domain.User;
import pl.studyshare.dto.TaskCreateRequest;
import pl.studyshare.dto.TaskDTO;
import pl.studyshare.dto.TaskUpdateRequest;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.repository.UserRepository;
import pl.studyshare.service.TaskService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskApiController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Page<TaskDTO>> getTasks(
            @RequestParam(required = false) LocalDate since,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort sorting = Sort.by(sortParts[0]);
        if (sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1])) {
            sorting = sorting.ascending();
        } else {
            sorting = sorting.descending();
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<TaskDTO> tasks = taskService.findByFilters(since, categoryId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody @Valid TaskCreateRequest request,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TaskDTO response = taskService.createPendingTask(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id,
                                              @RequestBody @Valid TaskUpdateRequest request,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User currentUser = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Zalogowany użytkownik nie istnieje"));
        TaskDTO response = taskService.updateTask(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // If the user isn't found in DB (e.g. mock user in tests), treat as forbidden
        User currentUser = userRepository.findByLogin(userDetails.getUsername())
                .orElse(null);

        pl.studyshare.domain.Task task = taskService.findEntityById(id);

        boolean isAdmin = currentUser != null && currentUser.getRole() == pl.studyshare.enums.Role.ADMIN;
        boolean isAuthor = currentUser != null && task.getAuthor() != null
                && task.getAuthor().getLogin().equals(currentUser.getLogin());

        if (!isAdmin && !isAuthor) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveTask(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Zalogowany użytkownik nie istnieje"));
        taskService.approveTask(id, admin);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectTask(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Zalogowany użytkownik nie istnieje"));
        TaskUpdateRequest updateRequest = new TaskUpdateRequest(null, null, null, TaskStatus.REJECTED, null);
        taskService.updateTask(id, updateRequest, admin);
        return ResponseEntity.noContent().build();
    }
}
