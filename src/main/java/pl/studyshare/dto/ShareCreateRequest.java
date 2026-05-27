package pl.studyshare.dto;

import jakarta.validation.constraints.NotNull;

public record ShareCreateRequest(
        @NotNull(message = "ID zadania jest wymagane")
        Long taskId,
        Long recipientUserId  
) {}