package pl.studyshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.studyshare.domain.Vote;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByAnswerIdAndVoterLogin(Long answerId, String login);
    void deleteByAnswerIdAndVoterLogin(Long answerId, String login);
    long countByAnswerIdAndVoteType(Long answerId, pl.studyshare.enums.VoteType voteType);
}
