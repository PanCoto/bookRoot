package pl.studyshare.dto;

import java.time.LocalDate;
import java.util.List;

public record AnswerDTO(
        Long id,
        String content,
        LocalDate createdDate,
        int score,
        String authorName,
        String authorAvatarFilename,
        boolean isOfficial,
        int commentCount,
        List<CommentDTO> comments
) {}
