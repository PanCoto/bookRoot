package pl.studyshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.studyshare.domain.Share;
import pl.studyshare.domain.Task;
import pl.studyshare.domain.User;

import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, Long> {
    Optional<Share> findByToken(String token);
    void deleteByTaskAndRecipient(Task task, User recipient);
    java.util.List<Share> findByTaskId(Long taskId);
}
