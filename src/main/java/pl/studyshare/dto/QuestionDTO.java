package pl.studyshare.dto;

import pl.studyshare.enums.QuestionType;

import java.util.List;

public record QuestionDTO(
        Long id,
        String content,
        QuestionType type,
        List<String> options,
        String correctAnswer,
        int points
) {}
