package pl.studyshare.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.*;
import pl.studyshare.dto.VoteCollector;
import pl.studyshare.dto.VoteRequest;
import pl.studyshare.enums.Role;
import pl.studyshare.enums.VoteType;
import pl.studyshare.repository.*;
import pl.studyshare.service.SessionFlushService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class VoteApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private SessionFlushService sessionFlushService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Answer testAnswer;

    @BeforeEach
    public void setup() {

        testUser = userRepository.save(User.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .login("jankowalski")
                .password("encoded_pass")
                .age(20)
                .role(Role.USER)
                .enabled(true)
                .build());

        Category testCategory = categoryRepository.save(Category.builder()
                .name("Analiza Matematyczna")
                .description("Matematyka wyższa")
                .build());

        Task testTask = taskRepository.save(Task.builder()
                .title("Zadanie domowe 1")
                .content("Oblicz granicę ciągu o wyrazie ogólnym...")
                .category(testCategory)
                .author(testUser)
                .createdDate(LocalDate.now())
                .build());

        testAnswer = answerRepository.save(Answer.builder()
                .content("Oto poprawna odpowiedź: granica wynosi 2.")
                .task(testTask)
                .author(testUser)
                .createdDate(LocalDate.now())
                .upvotes(0)
                .downvotes(0)
                .build());
    }

    @Test
    @WithMockUser(username = "jankowalski", roles = "USER")
    public void shouldRegisterVoteInSessionOnlyAndThenFlushToDbOnSessionFlush() throws Exception {
        VoteRequest voteRequest = new VoteRequest(testAnswer.getId(), VoteType.UPVOTE);

        MockHttpSession session = new MockHttpSession();

        MvcResult result = mockMvc.perform(post("/api/votes")
                        .with(csrf())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isOk())
                .andReturn();

        VoteCollector collector = (VoteCollector) session.getAttribute("voteCollector");
        assertThat(collector).isNotNull();
        assertThat(collector.getVotes().get(testAnswer.getId())).isEqualTo(VoteType.UPVOTE);

        Optional<Vote> dbVoteBeforeFlush = voteRepository.findByAnswerIdAndVoterLogin(testAnswer.getId(), "jankowalski");
        assertThat(dbVoteBeforeFlush).isNotPresent();

        sessionFlushService.flushVotes(session, "jankowalski");

        Optional<Vote> dbVoteAfterFlush = voteRepository.findByAnswerIdAndVoterLogin(testAnswer.getId(), "jankowalski");
        assertThat(dbVoteAfterFlush).isPresent();
        assertThat(dbVoteAfterFlush.get().getVoteType()).isEqualTo(VoteType.UPVOTE);

        Answer updatedAnswer = answerRepository.findById(testAnswer.getId()).orElseThrow();
        assertThat(updatedAnswer.getUpvotes()).isEqualTo(1);
        assertThat(updatedAnswer.getScore()).isEqualTo(1);
    }

    @Test
    public void shouldReturnForbiddenOrUnauthorizedForAnonymousUser() throws Exception {
        VoteRequest voteRequest = new VoteRequest(testAnswer.getId(), VoteType.UPVOTE);

        mockMvc.perform(post("/api/votes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().is3xxRedirection());
    }
}
