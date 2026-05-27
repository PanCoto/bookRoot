package pl.studyshare.mapper;

import org.springframework.stereotype.Component;
import pl.studyshare.domain.Task;
import pl.studyshare.dto.TaskDTO;

@Component
public class TaskMapper {
    public TaskDTO toDto(Task task) {
        String authorName = null;
        if (!task.getAnonymous() && task.getAuthor() != null) {
            authorName = task.getAuthor().getLogin();
        }
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getContent(),
                task.getImageUrl(),
                task.getStatus(),
                task.getCreatedDate(),
                authorName,
                task.getCategory() != null ? task.getCategory().getName() : null,
                task.getQuestions() != null ? task.getQuestions().size() : 0,
                task.getAnswers() != null ? task.getAnswers().size() : 0,
                0
        );
    }
}