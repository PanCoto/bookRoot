package pl.studyshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.studyshare.domain.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category c LEFT JOIN c.tasks t GROUP BY c.id ORDER BY COUNT(t.id) DESC")
    List<Category> findAllOrderByTaskCountDesc();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.category = :category")
    long countByCategory(@Param("category") Category category);
}
