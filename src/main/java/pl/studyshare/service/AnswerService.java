package pl.studyshare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.Answer;
import pl.studyshare.domain.Task;
import pl.studyshare.domain.User;
import pl.studyshare.dto.AnswerCreateRequest;
import pl.studyshare.dto.AnswerDTO;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.mapper.AnswerMapper;
import pl.studyshare.repository.AnswerRepository;
import pl.studyshare.repository.TaskRepository;
import pl.studyshare.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AnswerMapper answerMapper;

    @Transactional(readOnly = true)
    public List<AnswerDTO> findByTaskId(Long taskId) {
        return answerRepository.findByTaskIdWithSorting(taskId).stream()
                .map(answerMapper::toDto)
                .collect(Collectors.toList());
    }

    public AnswerDTO saveAnswer(Long taskId, AnswerCreateRequest request, String currentUserLogin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Zadanie nie istnieje"));
        if (task.getStatus() != TaskStatus.APPROVED) {
            throw new IllegalStateException("Można odpowiadać tylko na zatwierdzone zadania");
        }
        User author = userRepository.findByLogin(currentUserLogin)
                .orElseThrow(() -> new IllegalArgumentException("Nieznany użytkownik"));

        Answer answer = Answer.builder()
                .content(request.content())
                .anonymous(request.anonymous() != null ? request.anonymous() : true)
                .author(author)
                .task(task)
                .build();

        Answer saved = answerRepository.save(answer);
        return answerMapper.toDto(saved);
    }
}