package pl.studyshare.dto;

import jakarta.validation.constraints.*;

public record UserUpdateRequest(

        @NotBlank @Size(min = 3, max = 20, message = "Imię musi mieć 3-20 znaków")
        @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]*$",
                 message = "Imię musi zaczynać się wielką literą i zawierać tylko litery")
        String firstName,

        @NotBlank @Size(min = 3, max = 50, message = "Nazwisko musi mieć 3-50 znaków")
        @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]*$",
                 message = "Nazwisko musi zaczynać się wielką literą i zawierać tylko litery")
        String lastName,

        @Min(value = 18, message = "Wiek musi wynosić co najmniej 18 lat")
        int age,

        @Size(max = 80, message = "Nazwa wyświetlana może mieć maksymalnie 80 znaków")
        String displayName,

        @Email(message = "Podany adres e-mail jest nieprawidłowy")
        String email,

        Boolean anonymousMode
) {}
