package pl.studyshare.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import pl.studyshare.enums.Role;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 3, max = 20)
    @Pattern(regexp = "^[A-Z][a-z]*$")
    private String firstName;

    @NotBlank @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Z][a-z]*$")
    private String lastName;

    @NotBlank @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-z]+$")
    @Column(unique = true, nullable = false)
    private String login;

    @NotBlank @Size(min = 5)
    private String password;

    @Min(18)
    private int age;

    @Size(max = 80, message = "Nazwa wyświetlana może mieć maksymalnie 80 znaków")
    private String displayName;

    @Email(message = "Podany adres e-mail jest nieprawidłowy.")
    @Column(unique = true)
    private String email;

    @Builder.Default
    private Boolean anonymousMode = false;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @Builder.Default
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "voter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vote> votes = new ArrayList<>();
}
