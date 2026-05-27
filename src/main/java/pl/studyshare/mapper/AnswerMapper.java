package pl.studyshare.mapper;

import org.springframework.stereotype.Component;
import pl.studyshare.domain.Answer;
import pl.studyshare.dto.AnswerDTO;

@Component
public class AnswerMapper {
    public AnswerDTO toDto(Answer a) {
        return new AnswerDTO(
                a.getId(),
                a.getContent(),
                a.getCreatedDate(),
                a.getScore() != null ? a.getScore() : 0,
                a.getAnonymous() ? null : a.getAuthor().getLogin(),
                a.getIsOfficial(),
                0
        );
    }
}