package pl.studyshare.dto;

import jakarta.validation.constraints.*;


public class UserUpdateRequest {

    @NotBlank(message = "Imię jest wymagane")
    @Size(min = 2, max = 40, message = "Imię musi mieć 2-40 znaków")
    @Pattern(
        regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+(?:[\\s'\\-][A-ZĄĆĘŁŃÓŚŹŻ]?[a-ząćęłńóśźż]+)*$",
        message = "Imię musi zaczynać się wielką literą i zawierać tylko litery"
    )
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(min = 2, max = 60, message = "Nazwisko musi mieć 2-60 znaków")
    @Pattern(
        regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ][a-ząćęłńóśźż]+(?:[\\s'\\-][A-ZĄĆĘŁŃÓŚŹŻ]?[a-ząćęłńóśźż]+)*$",
        message = "Nazwisko musi zaczynać się wielką literą i zawierać tylko litery"
    )
    private String lastName;

    @Min(value = 18, message = "Wiek musi wynosić co najmniej 18 lat")
    @Max(value = 120, message = "Wiek nie może przekraczać 120 lat")
    private int age;

    @Size(max = 80, message = "Nazwa wyświetlana może mieć maksymalnie 80 znaków")
    private String displayName;

    @Email(message = "Podany adres e-mail jest nieprawidłowy")
    private String email;

    private Boolean anonymousMode;

    private String avatarFilename;


    public UserUpdateRequest() {}

    public UserUpdateRequest(String firstName, String lastName, int age,
                             String displayName, String email,
                             Boolean anonymousMode, String avatarFilename) {
        this.firstName     = firstName;
        this.lastName      = lastName;
        this.age           = age;
        this.displayName   = displayName;
        this.email         = email;
        this.anonymousMode = anonymousMode;
        this.avatarFilename = avatarFilename;
    }


    public String getFirstName()    { return firstName; }
    public String getLastName()     { return lastName; }
    public int    getAge()          { return age; }
    public String getDisplayName()  { return displayName; }
    public String getEmail()        { return email; }
    public Boolean getAnonymousMode() { return anonymousMode; }
    public String getAvatarFilename() { return avatarFilename; }

    public void setFirstName(String firstName)       { this.firstName = firstName; }
    public void setLastName(String lastName)         { this.lastName = lastName; }
    public void setAge(int age)                      { this.age = age; }
    public void setDisplayName(String displayName)   { this.displayName = displayName; }
    public void setEmail(String email)               { this.email = email; }
    public void setAnonymousMode(Boolean anonymousMode) { this.anonymousMode = anonymousMode; }
    public void setAvatarFilename(String avatarFilename) { this.avatarFilename = avatarFilename; }
}
