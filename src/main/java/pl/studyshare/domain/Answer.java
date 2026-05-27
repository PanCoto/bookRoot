// ==============================================================================
// Plik: src/main/java/pl/studyshare/domain/Answer.java
// Faza: F1
// REQ_IDS: DOMAIN_MODEL, Answer_ENTITY, FORMULA_FIELD, CRITICAL_09
// ==============================================================================
package pl.studyshare.domain;

import lombok.*;
import org.hibernate.annotations.Formula;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(name = "answers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Answer implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Treść odpowiedzi nie może być pusta")
    @Size(min = 10, max = 2000, message = "Odpowiedź musi mieć 10-2000 znaków")
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Builder.Default
    private LocalDate createdDate = LocalDate.now();

    @Builder.Default
    private Integer upvotes = 0;

    @Builder.Default
    private Integer downvotes = 0;

    // Pole wyliczane: upvotes - downvotes. Tylko do odczytu (brak settera).
    @Formula("upvotes - downvotes")
    private Integer score;

    @Builder.Default
    private Boolean anonymous = true;

    @Builder.Default
    private Boolean isOfficial = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}