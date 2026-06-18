package pl.studyshare.mapper;

import org.springframework.stereotype.Component;
import pl.studyshare.domain.Answer;
import pl.studyshare.dto.AnswerDTO;
import pl.studyshare.dto.CommentDTO;

import java.util.List;

@Component
public class AnswerMapper {
    public AnswerDTO toDto(Answer answer) {
        boolean isAnon = Boolean.TRUE.equals(answer.getAnonymous()) || answer.getAuthor() == null;
        String authorName   = isAnon ? null : answer.getAuthor().getLogin();
        String authorAvatar = isAnon ? null : answer.getAuthor().getAvatarFilename();
        return new AnswerDTO(
                answer.getId(),
                answer.getContent(),
                answer.getCreatedDate(),
                answer.getScore() != null ? answer.getScore() : 0,
                authorName,
                authorAvatar,
                Boolean.TRUE.equals(answer.getIsOfficial()),
                0,
                List.of()
        );
    }

    public AnswerDTO toDto(Answer answer, List<CommentDTO> comments) {
        boolean isAnon = Boolean.TRUE.equals(answer.getAnonymous()) || answer.getAuthor() == null;
        String authorName   = isAnon ? null : answer.getAuthor().getLogin();
        String authorAvatar = isAnon ? null : answer.getAuthor().getAvatarFilename();
        return new AnswerDTO(
                answer.getId(),
                answer.getContent(),
                answer.getCreatedDate(),
                answer.getScore() != null ? answer.getScore() : 0,
                authorName,
                authorAvatar,
                Boolean.TRUE.equals(answer.getIsOfficial()),
                comments.size(),
                comments
        );
    }
}