package pl.studyshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.studyshare.domain.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAnswerIdOrderByCreatedDateAsc(Long answerId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.answer a JOIN FETCH a.task t WHERE c.author.login = :login ORDER BY c.createdDate DESC")
    List<Comment> findByAuthorLoginOrderByCreatedDateDesc(@Param("login") String login);
}
