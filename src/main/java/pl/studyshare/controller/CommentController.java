package pl.studyshare.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.studyshare.service.CommentService;

@Controller
@RequestMapping("/tasks/{taskId}/answers/{answerId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public String addComment(@PathVariable Long taskId,
                             @PathVariable Long answerId,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            try {
                commentService.addComment(answerId, content, userDetails.getUsername());
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("commentError", e.getMessage());
                redirectAttributes.addFlashAttribute("commentErrorAnswerId", answerId);
            }
        }
        return "redirect:/tasks/" + taskId + "#answers";
    }
}
