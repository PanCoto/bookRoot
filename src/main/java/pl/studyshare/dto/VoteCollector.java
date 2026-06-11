package pl.studyshare.dto;

import pl.studyshare.enums.VoteType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VoteCollector implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Long, VoteType> votes = new HashMap<>();

    public Map<Long, VoteType> getVotes() {
        return votes;
    }

    public void collect(Long answerId, VoteType voteType) {
        if (voteType == null) {
            votes.remove(answerId);
        } else {
            votes.put(answerId, voteType);
        }
    }
}
