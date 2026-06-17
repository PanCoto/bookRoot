package pl.studyshare.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;

@Entity
@Table(name = "answers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Answer implements java.io.Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 10, max = 2000)
    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull
    @Builder.Default
    private LocalDate createdDate = LocalDate.now();

    @Builder.Default
    private Integer upvotes = 0;

    @Builder.Default
    private Integer downvotes = 0;

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
