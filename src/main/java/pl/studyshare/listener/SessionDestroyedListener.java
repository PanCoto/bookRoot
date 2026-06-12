package pl.studyshare.listener;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import pl.studyshare.service.SessionFlushService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionDestroyedListener implements HttpSessionListener {

    private final SessionFlushService sessionFlushService;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContext != null && securityContext.getAuthentication() != null) {
            String username = securityContext.getAuthentication().getName();
            log.info("Session expired/destroyed. Flushing votes for user: {}", username);
            try {
                sessionFlushService.flushVotes(session, username);
            } catch (Exception e) {
                log.error("Failed to flush session votes on session destruction", e);
            }
        }
    }
}
