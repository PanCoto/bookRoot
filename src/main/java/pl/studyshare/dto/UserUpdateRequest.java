package pl.studyshare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank @Size(min = 3, max = 20, message = "Imię musi mieć 3-20 znaków")
        @Pattern(regexp = "^[A-Z][a-z]*$", message = "Imię musi zaczynać się wielką literą i zawierać tylko litery")
        String firstName,

        @NotBlank @Size(min = 3, max = 50, message = "Nazwisko musi mieć 3-50 znaków")
        @Pattern(regexp = "^[A-Z][a-z]*$", message = "Nazwisko musi zaczynać się wielką literą i zawierać tylko litery")
        String lastName,

        @Min(value = 18, message = "Wiek musi wynosić co najmniej 18 lat")
        int age
) {}