package pl.studyshare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @NotBlank(message = "Imię nie może być puste")
        @Size(min = 3, max = 20, message = "Imię musi mieć 3-20 znaków")
        @Pattern(regexp = "^[A-Z][a-z]*$", message = "Imię musi zaczynać się wielką literą i zawierać tylko litery")
        String firstName,

        @NotBlank(message = "Nazwisko nie może być puste")
        @Size(min = 3, max = 50, message = "Nazwisko musi mieć 3-50 znaków")
        @Pattern(regexp = "^[A-Z][a-z]*$", message = "Nazwisko musi zaczynać się wielką literą i zawierać tylko litery")
        String lastName,

        @NotBlank(message = "Login nie może być pusty")
        @Size(min = 3, max = 20, message = "Login musi mieć 3-20 znaków")
        @Pattern(regexp = "^[a-z]+$", message = "Login musi składać się wyłącznie z małych liter")
        String login,

        @NotBlank(message = "Hasło nie może być puste")
        @Size(min = 5, message = "Hasło musi mieć co najmniej 5 znaków")
        String password,

        @Min(value = 18, message = "Wiek musi wynosić co najmniej 18 lat")
        int age
) {}
