package pl.studyshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.studyshare.domain.Answer;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("SELECT a FROM Answer a WHERE a.task.id = :taskId " +
            "ORDER BY CASE WHEN a.isOfficial = true THEN 0 ELSE 1 END, " +
            "a.score DESC, a.createdDate ASC")
    List<Answer> findByTaskIdWithSorting(@Param("taskId") Long taskId);

    List<Answer> findByTaskId(Long taskId);

    @Query("SELECT COALESCE(SUM(a.score), 0) FROM Answer a WHERE a.author.id = :userId")
    int sumScoreByAuthorId(@Param("userId") Long userId);
}
