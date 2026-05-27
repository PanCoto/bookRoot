package pl.studyshare.dto;

public record VoteResponse(
        Long answerId,
        int currentScore,
        int userVoteDeltaInSession
) {}