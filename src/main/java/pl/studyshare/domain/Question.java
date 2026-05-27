package pl.studyshare.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pl.studyshare.enums.QuestionType;
import pl.studyshare.validation.QuestionOptionsValid;

@Entity
@Table(name = "questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@QuestionOptionsValid
public class Question implements java.io.Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    private QuestionType type = QuestionType.MULTI_CHOICE;

    @Column(columnDefinition = "TEXT")
    private String options;

    @NotBlank
    private String correctAnswer;

    @Min(1)
    @Builder.Default
    private int points = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}