package pl.studyshare.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.enums.TaskType;
import pl.studyshare.validation.ValidOptionsJson;

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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 5, max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank @Size(min = 10, max = 5000)
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Optional URL linking to the source of the task (V5 validation) */
    @URL(message = "Podany adres URL źródła jest nieprawidłowy.")
    private String sourceUrl;

    /** Legacy image URL field – kept for backward compatibility */
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    private TaskStatus status = TaskStatus.DRAFT;

    /**
     * Type of task/question (YAML: task_types).
     * OPEN, CLOSED, MULTIPLE_CHOICE, TRUE_FALSE.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskType taskType = TaskType.OPEN;

    /**
     * JSON array of answer options for MULTIPLE_CHOICE and TRUE_FALSE types.
     * Format: [{"label": "Option A", "correct": true}, ...]
     * Validated by @ValidOptionsJson (V6).
     */
    @ValidOptionsJson
    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    /**
     * True if this task was added or approved by an admin (OFFICIAL content).
     * False means it is COMMUNITY content.
     */
    @Builder.Default
    private Boolean isOfficial = false;

    /** Number of views this task has received */
    @Min(0)
    @Builder.Default
    private int viewCount = 0;

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