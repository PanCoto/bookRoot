package pl.studyshare.api;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.studyshare.dto.VoteRequest;
import pl.studyshare.dto.VoteResponse;
import pl.studyshare.service.VoteService;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteApiController {

    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<VoteResponse> vote(@RequestBody VoteRequest request,
                                             @AuthenticationPrincipal UserDetails userDetails,
                                             HttpSession session) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        VoteResponse response = voteService.registerVote(
                request.answerId(),
                request.voteType(),
                userDetails.getUsername(),
                session
        );

        return ResponseEntity.ok(response);
    }
}
