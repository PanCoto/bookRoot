package pl.studyshare.domain;

import jakarta.persistence.Table;
import lombok.*;
import pl.studyshare.enums.Role;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Imię nie może być puste")
    @Size(min = 3, max = 20, message = "Imię musi mieć 3-20 znaków")
    @Pattern(regexp = "^[A-Z][a-z]*$", message = "Imię musi zaczynać się wielką literą i zawierać tylko litery")
    private String firstName;

    @NotBlank(message = "Nazwisko nie może być puste")
    @Size(min = 3, max = 50, message = "Nazwisko musi mieć 3-50 znaków")
    @Pattern(regexp = "^[A-Z][a-z]*$", message = "Nazwisko musi zaczynać się wielką literą i zawierać tylko litery")
    private String lastName;

    @NotBlank(message = "Login nie może być pusty")
    @Size(min = 3, max = 20, message = "Login musi mieć 3-20 znaków")
    @Pattern(regexp = "^[a-z]+$", message = "Login może zawierać tylko małe litery")
    @Column(unique = true, nullable = false)
    private String login;

    @NotBlank(message = "Hasło nie może być puste")
    @Size(min = 5, message = "Hasło musi mieć co najmniej 5 znaków")
    private String password; // będzie kodowane BCrypt przed zapisem

    @Min(value = 18, message = "Użytkownik musi mieć co najmniej 18 lat")
    private int age;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Rola jest wymagana")
    private Role role;

    @Builder.Default
    private Boolean enabled = true;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();
}