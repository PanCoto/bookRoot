package pl.studyshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import pl.studyshare.enums.TaskType;

public record TaskCreateRequest(
        @NotBlank(message = "Tytuł jest wymagany")
        @Size(min = 5, max = 200, message = "Tytuł musi mieć 5-200 znaków")
        String title,

        @NotBlank(message = "Treść jest wymagana")
        @Size(min = 10, max = 5000, message = "Treść musi mieć 10-5000 znaków")
        String content,

        String imageUrl,

        Long categoryId,

        Boolean anonymous,

        @URL(message = "Podany adres URL źródła jest nieprawidłowy.")
        String sourceUrl,

        TaskType taskType
) {}