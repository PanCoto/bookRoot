package pl.studyshare.mapper;

import org.springframework.stereotype.Component;
import pl.studyshare.domain.Answer;
import pl.studyshare.dto.AnswerDTO;

@Component
public class AnswerMapper {
    public AnswerDTO toDto(Answer answer) {
        String authorName = null;
        if (!answer.getAnonymous() && answer.getAuthor() != null) {
            authorName = answer.getAuthor().getLogin();
        }
        return new AnswerDTO(
                answer.getId(),
                answer.getContent(),
                answer.getCreatedDate(),
                answer.getScore() != null ? answer.getScore() : 0,
                authorName,
                Boolean.TRUE.equals(answer.getIsOfficial()),
                0
        );
    }
}