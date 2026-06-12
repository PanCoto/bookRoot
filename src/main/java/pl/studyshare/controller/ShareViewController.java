package pl.studyshare.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.studyshare.dto.TaskDTO;
import pl.studyshare.service.ShareService;

@Controller
@RequestMapping("/share")
@RequiredArgsConstructor
@Slf4j
public class ShareViewController {

    private final ShareService shareService;

    @GetMapping("/{token}")
    public String viewSharedTask(@PathVariable String token,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        String currentUsername = (userDetails != null) ? userDetails.getUsername() : null;
        try {
            TaskDTO task = shareService.findSharedTaskByToken(token, currentUsername);
            model.addAttribute("task", task);
            return "share-view";
        } catch (SecurityException e) {
            log.warn("Access denied to shared task with token {}: {}", token, e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        } catch (Exception e) {
            log.error("Error viewing shared task with token {}: {}", token, e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
