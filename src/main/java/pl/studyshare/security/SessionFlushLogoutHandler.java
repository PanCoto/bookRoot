package pl.studyshare.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import pl.studyshare.service.SessionFlushService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionFlushLogoutHandler implements LogoutHandler {

    private final SessionFlushService sessionFlushService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            HttpSession session = request.getSession(false);
            if (session != null) {
                log.info("User logged out. Flushing votes for user: {}", username);
                try {
                    sessionFlushService.flushVotes(session, username);
                } catch (Exception e) {
                    log.error("Failed to flush session votes on logout", e);
                }
            }
        }
    }
}
