package pl.studyshare.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for updating a user's own profile.
 * Maps to: ProfileController.updateProfile()
 * Fields align with YAML: User.displayName, User.email, User.anonymousMode
 */
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

        /** Displayed to other users when anonymousMode=false. YAML: User.displayName @Size(max=80) */
        @Size(max = 80, message = "Nazwa wyświetlana może mieć maksymalnie 80 znaków")
        String displayName,

        /** Optional email. YAML: User.email @Email @Unique */
        @Email(message = "Podany adres e-mail jest nieprawidłowy")
        String email,

        /** When true, hides the user's name on all public content. */
        Boolean anonymousMode
) {}