package pl.studyshare.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import pl.studyshare.enums.ShareType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a share token that gives access to a task draft.
 * Supports two share types: PUBLIC_LINK and SPECIFIC_USER.
 */
@Entity
@Table(name = "shares")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Share implements java.io.Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique random token used in the share URL. */
    @NotBlank
    @Column(unique = true, nullable = false)
    @Builder.Default
    private String token = UUID.randomUUID().toString();

    /** Whether this is a public link or targeted to a specific user. */
    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    private ShareType shareType = ShareType.PUBLIC_LINK;

    /** Optional expiry timestamp for the link. */
    private LocalDateTime expiresAt;

    /** Timestamp when the share was created. */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** The task being shared. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** The user who generated the share link. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Optional target user – populated when shareType = SPECIFIC_USER.
     * When set, only this user can access the shared task.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    /** Convenience check: has this link expired? */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}