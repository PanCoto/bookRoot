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
import pl.studyshare.dto.ShareCreateRequest;
import pl.studyshare.dto.ShareTokenDTO;
import pl.studyshare.enums.Role;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.repository.*;
import pl.studyshare.service.ShareService;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ShareApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ShareService shareService;

    @Autowired
    private ObjectMapper objectMapper;

    private User authorUser;
    private User recipientUser;
    private User otherUser;
    private Task draftTask;
    private Task approvedTask;

    @BeforeEach
    public void setup() {
        authorUser = userRepository.save(User.builder()
                .firstName("Anna")
                .lastName("Nowak")
                .login("annanowak")
                .password("pass1")
                .age(22)
                .role(Role.USER)
                .enabled(true)
                .build());

        recipientUser = userRepository.save(User.builder()
                .firstName("Tomasz")
                .lastName("Kot")
                .login("tomaszkot")
                .password("pass1")
                .age(25)
                .role(Role.USER)
                .enabled(true)
                .build());

        otherUser = userRepository.save(User.builder()
                .firstName("Jan")
                .lastName("Kos")
                .login("jankos")
                .password("pass1")
                .age(24)
                .role(Role.USER)
                .enabled(true)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Algebra Liniowa")
                .description("Algebra")
                .build());

        draftTask = taskRepository.save(Task.builder()
                .title("Szkic Zadania")
                .content("Treść szkicu...")
                .category(category)
                .author(authorUser)
                .status(TaskStatus.DRAFT)
                .createdDate(LocalDate.now())
                .build());

        approvedTask = taskRepository.save(Task.builder()
                .title("Zatwierdzone Zadanie")
                .content("Treść zatwierdzonego zadania...")
                .category(category)
                .author(authorUser)
                .status(TaskStatus.APPROVED)
                .createdDate(LocalDate.now())
                .build());
    }

    @Test
    @WithMockUser(username = "annanowak", roles = "USER")
    public void shouldCreateShareTokenForAuthor() throws Exception {
        ShareCreateRequest request = new ShareCreateRequest(draftTask.getId(), null);

        mockMvc.perform(post("/api/shares")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.taskId").value(draftTask.getId()))
                .andExpect(jsonPath("$.publicUrl").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "jankos", roles = "USER")
    public void shouldNotCreateShareTokenForNonAuthor() throws Exception {
        ShareCreateRequest request = new ShareCreateRequest(draftTask.getId(), null);

        mockMvc.perform(post("/api/shares")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "annanowak", roles = "USER")
    public void shouldNotCreateShareTokenForNonDraftTask() throws Exception {
        ShareCreateRequest request = new ShareCreateRequest(approvedTask.getId(), null);

        mockMvc.perform(post("/api/shares")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldAccessPublicShareLinkWithoutAuthentication() throws Exception {
        ShareTokenDTO shareToken = shareService.createShareToken(new ShareCreateRequest(draftTask.getId(), null), "annanowak");

        mockMvc.perform(get("/share/" + shareToken.token()))
                .andExpect(status().isOk())
                .andExpect(view().name("sharel-view"))
                .andExpect(model().attributeExists("task"));
    }

    @Test
    @WithMockUser(username = "tomaszkot", roles = "USER")
    public void shouldAccessSpecificUserShareLinkWithCorrectUser() throws Exception {
        ShareTokenDTO shareToken = shareService.createShareToken(new ShareCreateRequest(draftTask.getId(), recipientUser.getId()), "annanowak");

        mockMvc.perform(get("/share/" + shareToken.token()))
                .andExpect(status().isOk())
                .andExpect(view().name("sharel-view"))
                .andExpect(model().attributeExists("task"));
    }

    @Test
    @WithMockUser(username = "jankos", roles = "USER")
    public void shouldNotAccessSpecificUserShareLinkWithWrongUser() throws Exception {
        ShareTokenDTO shareToken = shareService.createShareToken(new ShareCreateRequest(draftTask.getId(), recipientUser.getId()), "annanowak");

        mockMvc.perform(get("/share/" + shareToken.token()))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
