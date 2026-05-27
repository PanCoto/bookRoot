package pl.studyshare.domain;

import jakarta.validation.constraints.Pattern;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Category implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa kategorii nie może być pusta")
    @Size(min = 3, max = 50, message = "Nazwa kategorii musi mieć 3-50 znaków")
    @Pattern(regexp = "^[a-z]+$", message = "Nazwa kategorii może zawierać tylko małe litery")
    @Column(unique = true)
    private String name;

    @Size(max = 500, message = "Opis może mieć maksymalnie 500 znaków")
    private String description;

    @OneToMany(mappedBy = "category")
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    // Wygodny konstruktor dla DataSeeder
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
}