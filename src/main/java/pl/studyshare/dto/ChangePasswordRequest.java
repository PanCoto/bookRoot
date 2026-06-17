package pl.studyshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Hasło nie może być puste")
        String currentPassword,

        @NotBlank(message = "Nowe hasło nie może być puste")
        @Size(min = 8, message = "Nowe hasło musi mieć co najmniej 8 znaków")
        String newPassword,

        @NotBlank(message = "Potwierdzenie hasła nie może być puste")
        String confirmPassword
) {}
