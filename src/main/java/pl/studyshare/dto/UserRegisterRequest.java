package pl.studyshare.dto;

import jakarta.validation.constraints.*;

public record UserRegisterRequest(
        @NotBlank(message = "Imię jest wymagane")
        @Size(min = 3, max = 20, message = "Imię musi mieć 3-20 znaków")
        @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]*$", message = "Imię musi zaczynać się wielką literą i zawierać tylko litery")
        String firstName,

        @NotBlank(message = "Nazwisko jest wymagane")
        @Size(min = 3, max = 50, message = "Nazwisko musi mieć 3-50 znaków")
        @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]*$", message = "Nazwisko musi zaczynać się wielką literą i zawierać tylko litery")
        String lastName,

        @NotBlank(message = "Login jest wymagany")
        @Size(min = 3, max = 20, message = "Login musi mieć 3-20 znaków")
        @Pattern(regexp = "^[a-z]+$", message = "Login może zawierać tylko małe litery")
        String login,

        @Email(message = "Podany adres e-mail jest nieprawidłowy")
        String email,

        @NotBlank(message = "Hasło jest wymagane")
        @Size(min = 5, message = "Hasło musi mieć co najmniej 5 znaków")
        String password,

        @Min(value = 18, message = "Wiek musi wynosić co najmniej 18 lat")
        int age,

        @AssertTrue(message = "Musisz zaakceptować Warunki Użytkowania, aby założyć konto")
        boolean eulaAccepted
) {}
