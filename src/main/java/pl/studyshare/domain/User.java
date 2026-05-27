package pl.studyshare.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pl.studyshare.enums.Role;

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

    @Enumerated(EnumType.STRING)
    @NotNull
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