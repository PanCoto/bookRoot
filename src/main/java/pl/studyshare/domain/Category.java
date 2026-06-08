package pl.studyshare.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Category implements java.io.Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 2, max = 100, message = "Nazwa kategorii musi mieć 2-100 znaków")
    @Pattern(regexp = "^[A-ZĄĆĘŁŃÓŚŹŻ].*", message = "Nazwa kategorii musi zaczynać się wielką literą")
    @Column(unique = true)
    private String name;

    @Size(max = 500)
    private String description;

    /** Timestamp set automatically when the category is first persisted. */
    @CreationTimestamp
    @Column(updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
}