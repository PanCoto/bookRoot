package pl.studyshare.dto;

import pl.studyshare.enums.VoteType;

public record VoteRequest(
        Long answerId,
        VoteType voteType
) {}
