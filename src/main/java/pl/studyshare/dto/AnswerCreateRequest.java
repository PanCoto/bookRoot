package pl.studyshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerCreateRequest(
        @NotBlank @Size(min = 10, max = 2000, message = "Odpowiedź musi mieć 10-2000 znaków")
        String content,
        Boolean anonymous
) {}
