package pl.studyshare.service;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.Answer;
import pl.studyshare.domain.User;
import pl.studyshare.domain.Vote;
import pl.studyshare.dto.VoteCollector;
import pl.studyshare.enums.VoteType;
import pl.studyshare.repository.AnswerRepository;
import pl.studyshare.repository.UserRepository;
import pl.studyshare.repository.VoteRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SessionFlushService {

    private final VoteRepository voteRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public void flushVotes(HttpSession session, String username) {
        VoteCollector collector = (VoteCollector) session.getAttribute("voteCollector");
        if (collector == null || collector.getVotes().isEmpty()) {
            return;
        }

        User voter = userRepository.findByLogin(username)
                .orElse(null);
        if (voter == null) {
            log.warn("Cannot flush votes: User '{}' not found", username);
            return;
        }

        log.info("Flushing {} session votes for user: {}", collector.getVotes().size(), username);

        for (Map.Entry<Long, VoteType> entry : collector.getVotes().entrySet()) {
            Long answerId = entry.getKey();
            VoteType sessionVoteType = entry.getValue();

            Optional<Vote> existingVoteOpt = voteRepository.findByAnswerIdAndVoterLogin(answerId, username);

            if (existingVoteOpt.isPresent()) {
                Vote existingVote = existingVoteOpt.get();
                if (sessionVoteType == null) {
                    voteRepository.delete(existingVote);
                } else if (existingVote.getVoteType() != sessionVoteType) {
                    existingVote.setVoteType(sessionVoteType);
                    voteRepository.save(existingVote);
                }
            } else if (sessionVoteType != null) {
                Answer answer = answerRepository.findById(answerId).orElse(null);
                if (answer != null) {
                    Vote newVote = Vote.builder()
                            .answer(answer)
                            .voter(voter)
                            .voteType(sessionVoteType)
                            .createdAt(LocalDateTime.now())
                            .build();
                    voteRepository.save(newVote);
                }
            }

            updateAnswerAggregates(answerId);
        }

        collector.getVotes().clear();
    }

    private void updateAnswerAggregates(Long answerId) {
        Answer answer = answerRepository.findById(answerId).orElse(null);
        if (answer != null) {
            int upvotes = (int) voteRepository.countByAnswerIdAndVoteType(answerId, VoteType.UPVOTE);
            int downvotes = (int) voteRepository.countByAnswerIdAndVoteType(answerId, VoteType.DOWNVOTE);
            answer.setUpvotes(upvotes);
            answer.setDownvotes(downvotes);

            answerRepository.saveAndFlush(answer);
            entityManager.refresh(answer);
        }
    }
}
