package pl.studyshare.domain;

import lombok.*;
import pl.studyshare.enums.QuestionType;
import pl.studyshare.validation.QuestionOptionsValid;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@QuestionOptionsValid
public class Question implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Treść pytania nie może być pusta")
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    private QuestionType type = QuestionType.MULTI_CHOICE;

    // Przechowywane jako JSON string (np. '["A","B","C"]')
    @Column(columnDefinition = "TEXT")
    private String options;

    @NotBlank(message = "Poprawna odpowiedź jest wymagana")
    private String correctAnswer;

    @Min(value = 1, message = "Punkty muszą być >= 1")
    @Builder.Default
    private int points = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}