package pl.studyshare.dto;

import pl.studyshare.enums.TaskStatus;

import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
        @Size(min = 5, max = 150, message = "Tytuł musi mieć 5-150 znaków")
        String title,

        @Size(min = 20, max = 5000, message = "Treść musi mieć 20-5000 znaków")
        String content,

        String imageUrl,

        TaskStatus status,       // tylko admin może zmieniać

        Long categoryId
) {}