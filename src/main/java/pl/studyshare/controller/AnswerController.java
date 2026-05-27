package pl.studyshare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.studyshare.dto.AnswerCreateRequest;
import pl.studyshare.service.AnswerService;

@Controller
@RequestMapping("/tasks/{taskId}/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping
    public String addAnswer(@PathVariable Long taskId,
                            @Valid @ModelAttribute("newAnswer") AnswerCreateRequest request,
                            @AuthenticationPrincipal UserDetails userDetails) {
        answerService.saveAnswer(taskId, request, userDetails.getUsername());
        return "redirect:/tasks/" + taskId + "#answers";
    }
}