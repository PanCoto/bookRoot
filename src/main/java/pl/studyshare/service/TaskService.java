package pl.studyshare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.Category;
import pl.studyshare.domain.Task;
import pl.studyshare.domain.User;
import pl.studyshare.dto.TaskCreateRequest;
import pl.studyshare.dto.TaskDTO;
import pl.studyshare.dto.TaskUpdateRequest;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.mapper.TaskMapper;
import pl.studyshare.repository.CategoryRepository;
import pl.studyshare.repository.TaskRepository;
import pl.studyshare.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public List<TaskDTO> findLatestApproved(int limit) {
        return taskRepository.findTop10ByStatusOrderByCreatedDateDesc(TaskStatus.APPROVED)
                .stream()
                .limit(limit)
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findByFilters(LocalDate since, Long categoryId, Pageable pageable) {
        Page<Task> tasks;
        if (categoryId != null) {
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Nieznana kategoria"));
            tasks = taskRepository.findByCategoryAndStatus(cat, TaskStatus.APPROVED, pageable);
        } else {
            tasks = taskRepository.findByStatus(TaskStatus.APPROVED, pageable);
        }
        if (since != null) {
            tasks = tasks.map(t -> t.getCreatedDate().isAfter(since) ? t : null);
        }
        return tasks.map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public TaskDTO findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Zadanie nie istnieje"));
        return taskMapper.toDto(task);
    }

    @Transactional(readOnly = true)
    public Task findEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Zadanie nie istnieje"));
    }

    public TaskDTO createTask(TaskCreateRequest request, String currentUserLogin) {
        User author = userRepository.findByLogin(currentUserLogin)
                .orElseThrow(() -> new IllegalArgumentException("Nieznany użytkownik"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Nieznana kategoria"));

        Task task = Task.builder()
                .title(request.title())
                .content(request.content())
                .imageUrl(request.imageUrl())
                .status(TaskStatus.DRAFT)
                .createdDate(LocalDate.now())
                .author(author)
                .category(category)
                .anonymous(request.anonymous() != null ? request.anonymous() : true)
                .build();

        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public TaskDTO updateTask(Long taskId, TaskUpdateRequest request, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Zadanie nie istnieje"));

        if (!currentUser.getRole().name().equals("ADMIN") &&
                !(task.getAuthor().equals(currentUser) && task.getStatus() == TaskStatus.DRAFT)) {
            throw new SecurityException("Brak uprawnień do edycji");
        }

        if (request.title() != null) task.setTitle(request.title());
        if (request.content() != null) task.setContent(request.content());
        if (request.imageUrl() != null) task.setImageUrl(request.imageUrl());

        if (request.categoryId() != null) {
            Category cat = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Nieznana kategoria"));
            task.setCategory(cat);
        }

        if (request.status() != null && currentUser.getRole().name().equals("ADMIN")) {
            task.setStatus(request.status());
            if (request.status() == TaskStatus.APPROVED) {
                task.setApprovedBy(currentUser);
            }
        }

        task.setLastModifiedDate(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    public void approveTask(Long taskId, User admin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Zadanie nie istnieje"));
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new IllegalStateException("Można zatwierdzić tylko zadania oczekujące");
        }
        task.setStatus(TaskStatus.APPROVED);
        task.setApprovedBy(admin);
        task.setLastModifiedDate(LocalDateTime.now());
        taskRepository.save(task);
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
}