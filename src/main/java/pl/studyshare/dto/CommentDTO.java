package pl.studyshare.dto;

import java.time.LocalDate;

public record CommentDTO(
        Long id,
        String content,
        LocalDate createdDate,
        String authorName
) {}
