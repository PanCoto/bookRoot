package pl.studyshare.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.*;
import pl.studyshare.dto.TaskCreateRequest;
import pl.studyshare.enums.Role;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.repository.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TaskRestApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User author;
    private User admin;
    private Category category;
    private Task approvedTask;

    @BeforeEach
    public void setup() {
        author = userRepository.save(User.builder()
                .firstName("Kamil")
                .lastName("Kowal")
                .login("kamilkowal")
                .password("pass1")
                .age(20)
                .role(Role.USER)
                .enabled(true)
                .build());

        admin = userRepository.save(User.builder()
                .firstName("Adam")
                .lastName("Admin")
                .login("adamadmin")
                .password("pass1")
                .age(30)
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        category = categoryRepository.save(Category.builder()
                .name("Fizyka")
                .description("Fizyka ogólna")
                .build());

        approvedTask = taskRepository.save(Task.builder()
                .title("Zadanie z termodynamiki")
                .content("Oblicz sprawność cyklu Carnota...")
                .category(category)
                .author(author)
                .status(TaskStatus.APPROVED)
                .createdDate(LocalDate.now())
                .build());
    }

    @Test
    public void shouldGetApprovedTasksList() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Zadanie z termodynamiki"));
    }

    @Test
    @WithMockUser(username = "kamilkowal", roles = "USER")
    public void shouldCreateTaskInPendingStatus() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest(
        "Nowe zadanie z optyki",
        "Oblicz kąt załamania światła na granicy ośrodków...",
        null,
        category.getId(),
        true,
        null,
        pl.studyshare.enums.TaskType.OPEN
);

        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Nowe zadanie z optyki"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "adamadmin", roles = "ADMIN")
    public void shouldApprovePendingTaskAsAdmin() throws Exception {
        Task pendingTask = taskRepository.save(Task.builder()
                .title("Zadanie do zatwierdzenia")
                .content("Treść zadania oczekującego...")
                .category(category)
                .author(author)
                .status(TaskStatus.PENDING)
                .createdDate(LocalDate.now())
                .build());

        mockMvc.perform(patch("/api/tasks/" + pendingTask.getId() + "/approve")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Task updated = taskRepository.findById(pendingTask.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.APPROVED);
        assertThat(updated.getApprovedBy().getLogin()).isEqualTo("adamadmin");
    }

    @Test
    @WithMockUser(username = "kamilkowal", roles = "USER")
    public void shouldDeleteOwnTask() throws Exception {
        Task ownTask = taskRepository.save(Task.builder()
                .title("Własne zadanie")
                .content("Treść własnego zadania...")
                .category(category)
                .author(author)
                .status(TaskStatus.DRAFT)
                .createdDate(LocalDate.now())
                .build());

        mockMvc.perform(delete("/api/tasks/" + ownTask.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(ownTask.getId())).isNotPresent();
    }

    @Test
    @WithMockUser(username = "jankos", roles = "USER")
    public void shouldNotDeleteOtherUserTask() throws Exception {
        Task otherUserTask = taskRepository.save(Task.builder()
                .title("Cudze zadanie")
                .content("Treść cudzego zadania...")
                .category(category)
                .author(author)
                .status(TaskStatus.DRAFT)
                .createdDate(LocalDate.now())
                .build());

        mockMvc.perform(delete("/api/tasks/" + otherUserTask.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
