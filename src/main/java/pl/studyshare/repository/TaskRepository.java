package pl.studyshare.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.studyshare.domain.Category;
import pl.studyshare.domain.Task;
import pl.studyshare.enums.TaskStatus;
import pl.studyshare.enums.TaskType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.author WHERE t.id = :id")
    Optional<Task> findByIdWithAuthor(@Param("id") Long id);

    List<Task> findTop10ByStatusOrderByCreatedDateDesc(TaskStatus status);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByCategoryAndStatus(Category category, TaskStatus status, Pageable pageable);

    List<Task> findByStatusAndCreatedDateAfter(TaskStatus status, LocalDate date);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.category = :category")
    long countByCategory(@Param("category") Category category);

    @Query("SELECT t FROM Task t LEFT JOIN t.answers a WHERE t.status = :status GROUP BY t.id, t.title, t.content, t.imageUrl, t.status, t.createdDate, t.lastModifiedDate, t.anonymous, t.author, t.category, t.approvedBy ORDER BY COUNT(a) DESC")
    Page<Task> findByStatusOrderByAnswersCountDesc(@Param("status") TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM Task t LEFT JOIN t.answers a WHERE t.status = :status GROUP BY t.id, t.title, t.content, t.imageUrl, t.status, t.createdDate, t.lastModifiedDate, t.anonymous, t.author, t.category, t.approvedBy ORDER BY COUNT(a) ASC")
    Page<Task> findByStatusOrderByAnswersCountAsc(@Param("status") TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM Task t LEFT JOIN t.answers a WHERE t.category = :category AND t.status = :status GROUP BY t.id, t.title, t.content, t.imageUrl, t.status, t.createdDate, t.lastModifiedDate, t.anonymous, t.author, t.category, t.approvedBy ORDER BY COUNT(a) DESC")
    Page<Task> findByCategoryAndStatusOrderByAnswersCountDesc(@Param("category") Category category, @Param("status") TaskStatus status, Pageable pageable);

    @Query("SELECT t FROM Task t LEFT JOIN t.answers a WHERE t.category = :category AND t.status = :status GROUP BY t.id, t.title, t.content, t.imageUrl, t.status, t.createdDate, t.lastModifiedDate, t.anonymous, t.author, t.category, t.approvedBy ORDER BY COUNT(a) ASC")
    Page<Task> findByCategoryAndStatusOrderByAnswersCountAsc(@Param("category") Category category, @Param("status") TaskStatus status, Pageable pageable);

    /** Returns all tasks with a specific status – used by admin moderation queue */
    List<Task> findAllByStatusOrderByCreatedDateAsc(TaskStatus status);

    /** Counts tasks by status – used by admin dashboard statistics */
    long countByStatus(TaskStatus status);

    // ── taskType filters ────────────────────────────────────────────────

    Page<Task> findByStatusAndTaskType(TaskStatus status, TaskType taskType, Pageable pageable);

    Page<Task> findByCategoryAndStatusAndTaskType(Category category, TaskStatus status, TaskType taskType, Pageable pageable);

    @Query("SELECT t FROM Task t LEFT JOIN t.answers a WHERE t.status = :status AND t.taskType = :taskType GROUP BY t.id ORDER BY COUNT(a) DESC")
    Page<Task> findByStatusAndTaskTypeOrderByAnswersCountDesc(@Param("status") TaskStatus status, @Param("taskType") TaskType taskType, Pageable pageable);

    @Query("SELECT t FROM Task t LEFT JOIN t.answers a WHERE t.status = :status AND t.taskType = :taskType GROUP BY t.id ORDER BY COUNT(a) ASC")
    Page<Task> findByStatusAndTaskTypeOrderByAnswersCountAsc(@Param("status") TaskStatus status, @Param("taskType") TaskType taskType, Pageable pageable);
}