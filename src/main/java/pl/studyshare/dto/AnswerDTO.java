package pl.studyshare.dto;

import java.time.LocalDate;

public record AnswerDTO(
        Long id,
        String content,
        LocalDate createdDate,
        int score,
        String authorName,
        boolean isOfficial,
        int commentCount
) {}