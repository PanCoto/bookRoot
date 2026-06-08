package pl.studyshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.studyshare.domain.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAnswerIdOrderByCreatedDateAsc(Long answerId);
}