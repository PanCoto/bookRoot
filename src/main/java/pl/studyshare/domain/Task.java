package pl.studyshare.domain;

import lombok.*;
import pl.studyshare.enums.TaskStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Task implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tytuł nie może być pusty")
    @Size(min = 5, max = 150, message = "Tytuł musi mieć 5-150 znaków")
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Treść nie może być pusta")
    @Size(min = 20, max = 5000, message = "Treść musi mieć 20-5000 znaków")
    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    private TaskStatus status = TaskStatus.DRAFT;

    @NotNull
    @Builder.Default
    private LocalDate createdDate = LocalDate.now();

    private LocalDateTime lastModifiedDate;

    @Builder.Default
    private Boolean anonymous = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Share> shares = new ArrayList<>();
}