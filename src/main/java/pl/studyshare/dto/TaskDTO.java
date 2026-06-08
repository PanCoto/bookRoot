package pl.studyshare.dto;

import pl.studyshare.enums.TaskStatus;
import pl.studyshare.enums.TaskType;
import java.time.LocalDate;

public record TaskDTO(
        Long id,
        String title,
        String content,
        String imageUrl,
        TaskStatus status,
        LocalDate createdDate,
        String authorName,
        String categoryName,
        int questionCount,
        int answerCount,
        long upvotesSummary,
        boolean anonymous,
        TaskType taskType,
        int viewCount,
        boolean isOfficial,
        String sourceUrl
) {}