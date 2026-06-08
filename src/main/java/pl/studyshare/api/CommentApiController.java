package pl.studyshare.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.studyshare.dto.CommentDTO;
import pl.studyshare.service.CommentService;

import java.util.List;

/**
 * REST API for comments on answers.
 *
 * GET    /api/answers/{answerId}/comments   – public list of comments
 * POST   /api/answers/{answerId}/comments   – add comment (USER, ADMIN)
 * DELETE /api/comments/{id}                  – delete own comment or any if ADMIN
 */
@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;

    @GetMapping("/api/answers/{answerId}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long answerId) {
        return ResponseEntity.ok(commentService.findByAnswerId(answerId));
    }

    @PostMapping("/api/answers/{answerId}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long answerId,
                                                  @RequestBody @Valid CommentCreateApiRequest request,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CommentDTO saved = commentService.addComment(answerId, request.content(),
                request.anonymous() != null && request.anonymous(),
                userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        commentService.deleteComment(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /** Inner request DTO – avoids creating a separate file for a simple payload. */
    record CommentCreateApiRequest(
            @jakarta.validation.constraints.NotBlank
            @jakarta.validation.constraints.Size(min = 2, max = 1000)
            String content,
            Boolean anonymous
    ) {}
}
