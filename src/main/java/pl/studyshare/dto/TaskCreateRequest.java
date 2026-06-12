package pl.studyshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import pl.studyshare.enums.TaskType;
import pl.studyshare.validation.ValidOptionsJson;

import java.util.List;

public record TaskCreateRequest(

        @NotBlank @Size(min = 5, max = 150, message = "Tytuł musi mieć 5-150 znaków")
        String title,

        @NotBlank @Size(min = 10, max = 5000, message = "Treść musi mieć 10-5000 znaków")
        String content,

        @URL(protocol = "http,https", message = "Nieprawidłowy adres URL obrazka")
        String imageUrl,

        @NotNull(message = "Kategoria jest wymagana")
        Long categoryId,

        Boolean anonymous,

        List<QuestionDTO> questions,

        TaskType taskType,

        @ValidOptionsJson
        String optionsJson,

        @URL(protocol = "http,https", message = "Nieprawidłowy adres URL źródła zadania")
        String sourceUrl

) {}
