package pl.studyshare.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.studyshare.dto.AnswerCreateRequest;
import pl.studyshare.dto.AnswerDTO;
import pl.studyshare.dto.AnswerUpdateRequest;
import pl.studyshare.service.AnswerService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnswerApiController {

    private final AnswerService answerService;

    @GetMapping("/api/tasks/{taskId}/answers")
    public ResponseEntity<List<AnswerDTO>> getAnswersByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(answerService.findByTaskId(taskId));
    }

    @PostMapping("/api/tasks/{taskId}/answers")
    public ResponseEntity<AnswerDTO> createAnswer(@PathVariable Long taskId,
                                                  @RequestBody @Valid AnswerCreateRequest request,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AnswerDTO response = answerService.saveAnswer(taskId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/answers/{id}")
    public ResponseEntity<AnswerDTO> updateAnswer(@PathVariable Long id,
                                                  @RequestBody @Valid AnswerUpdateRequest request,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AnswerDTO response = answerService.updateAnswer(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/answers/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        answerService.deleteAnswer(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/answers/{id}/official")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerDTO> markAsOfficial(@PathVariable Long id) {
        AnswerDTO updated = answerService.markAsOfficial(id);
        return ResponseEntity.ok(updated);
    }
}
