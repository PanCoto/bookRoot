package pl.studyshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank @Size(min = 3, max = 50, message = "Nazwa kategorii musi mieć 3-50 znaków")
        String name,
        String description
) {}
