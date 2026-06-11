package pl.studyshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import pl.studyshare.enums.TaskType;
import pl.studyshare.validation.ValidOptionsJson;

import java.util.List;


public record TaskCreateRequest(

        // V1
        @NotBlank @Size(min = 5, max = 150, message = "Tytuł musi mieć 5-150 znaków")
        String title,

        // V2 – min=10 to match Task entity constraint
        @NotBlank @Size(min = 10, max = 5000, message = "Treść musi mieć 10-5000 znaków")
        String content,

        // Legacy image URL (kept for backward compatibility)
        @URL(protocol = "http,https", message = "Nieprawidłowy adres URL obrazka")
        String imageUrl,

        // V3
        @NotNull(message = "Kategoria jest wymagana")
        Long categoryId,

        // V4
        Boolean anonymous,

        // Optional – existing question list support
        List<QuestionDTO> questions,

        // New: type of task (OPEN, CLOSED, MULTIPLE_CHOICE, TRUE_FALSE)
        TaskType taskType,

        // V6 – JSON array of options for MULTIPLE_CHOICE / TRUE_FALSE
        @ValidOptionsJson
        String optionsJson,

        // V5 – source URL (link to original exam/textbook page)
        @URL(protocol = "http,https", message = "Nieprawidłowy adres URL źródła zadania")
        String sourceUrl

) {}