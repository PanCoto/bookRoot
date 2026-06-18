package pl.studyshare.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.studyshare.domain.Answer;
import pl.studyshare.domain.Vote;
import pl.studyshare.dto.AnswerDTO;
import pl.studyshare.dto.VoteCollector;
import pl.studyshare.dto.VoteResponse;
import pl.studyshare.enums.VoteType;
import pl.studyshare.repository.AnswerRepository;
import pl.studyshare.repository.VoteRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final AnswerRepository answerRepository;

    public VoteResponse registerVote(Long answerId, VoteType newVoteType, String username, HttpSession session) {

        VoteCollector collector = (VoteCollector) session.getAttribute("voteCollector");
        if (collector == null) {
            collector = new VoteCollector();
            session.setAttribute("voteCollector", collector);
        }

        VoteType dbVote = voteRepository.findByAnswerIdAndVoterLogin(answerId, username)
                .map(Vote::getVoteType)
                .orElse(null);

        collector.collect(answerId, newVoteType);

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found: " + answerId));
        int dbScore = answer.getScore() != null ? answer.getScore() : 0;

        int dbVal = getVoteValue(dbVote);
        int sessionVal = getVoteValue(newVoteType);
        int currentAdjustedScore = dbScore - dbVal + sessionVal;

        int userVoteDeltaInSession = sessionVal;

        return new VoteResponse(answerId, currentAdjustedScore, userVoteDeltaInSession);
    }

    public List<AnswerDTO> getAdjustedAnswers(List<AnswerDTO> answers, String username, HttpSession session) {
        if (username == null) {
            return answers;
        }

        VoteCollector collector = (session != null) ? (VoteCollector) session.getAttribute("voteCollector") : null;
        List<AnswerDTO> adjusted = new ArrayList<>();

        for (AnswerDTO dto : answers) {
            VoteType sessionVote = (collector != null) ? collector.getVotes().get(dto.id()) : null;
            VoteType dbVote = voteRepository.findByAnswerIdAndVoterLogin(dto.id(), username)
                    .map(Vote::getVoteType)
                    .orElse(null);

            if (sessionVote != null) {
                int dbVal = getVoteValue(dbVote);
                int sessVal = getVoteValue(sessionVote);
                int adjustedScore = dto.score() - dbVal + sessVal;
                adjusted.add(new AnswerDTO(
                        dto.id(),
                        dto.content(),
                        dto.createdDate(),
                        adjustedScore,
                        dto.authorName(),
                        dto.authorAvatarFilename(),
                        dto.isOfficial(),
                        dto.commentCount(),
                        dto.comments()
                ));
            } else {
                adjusted.add(dto);
            }
        }

        return adjusted;
    }

    public Map<Long, String> getUserVotesMap(List<AnswerDTO> answers, String username, HttpSession session) {
        Map<Long, String> votesMap = new HashMap<>();
        if (username == null) {
            return votesMap;
        }

        VoteCollector collector = (session != null) ? (VoteCollector) session.getAttribute("voteCollector") : null;

        for (AnswerDTO dto : answers) {
            VoteType sessionVote = (collector != null) ? collector.getVotes().get(dto.id()) : null;
            if (sessionVote != null) {
                votesMap.put(dto.id(), sessionVote.name());
            } else {
                voteRepository.findByAnswerIdAndVoterLogin(dto.id(), username)
                        .map(Vote::getVoteType)
                        .ifPresent(v -> votesMap.put(dto.id(), v.name()));
            }
        }

        return votesMap;
    }

    private int getVoteValue(VoteType type) {
        if (type == null) return 0;
        return type == VoteType.UPVOTE ? 1 : -1;
    }
}