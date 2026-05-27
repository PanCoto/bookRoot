package pl.studyshare.mapper;

import org.springframework.stereotype.Component;
import pl.studyshare.domain.Task;
import pl.studyshare.dto.TaskDTO;

@Component
public class TaskMapper {
    public TaskDTO toDto(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getContent(),
                task.getImageUrl(),
                task.getStatus(),
                task.getCreatedDate(),
                task.getAnonymous() ? null : task.getAuthor().getLogin(),
                task.getCategory() != null ? task.getCategory().getName() : null,
                task.getQuestions().size(),
                task.getAnswers().size(),
                0
        );
    }
}