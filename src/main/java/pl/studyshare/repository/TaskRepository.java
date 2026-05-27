package pl.studyshare.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.studyshare.domain.Category;
import pl.studyshare.domain.Task;
import pl.studyshare.enums.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findTop10ByStatusOrderByCreatedDateDesc(TaskStatus status);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByCategoryAndStatus(Category category, TaskStatus status, Pageable pageable);

    List<Task> findByStatusAndCreatedDateAfter(TaskStatus status, LocalDate date);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.category = :category")
    long countByCategory(@Param("category") Category category);
}