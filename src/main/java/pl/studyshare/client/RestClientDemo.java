package pl.studyshare.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Demonstrates REST API usage for bookRoot endpoints.
 * Runs automatically on application startup in 'dev' profile.
 * YAML requirement: Klient REST – 2p.
 *
 * Demonstrates:
 *   1. GET  /api/tasks          – list of approved tasks (public)
 *   2. POST /api/tasks          – create task (requires auth – shown with note)
 *   3. POST /api/votes          – vote on answer (requires auth – shown with note)
 *   4. GET  /api/shares/{token} – fetch task by share token (public)
 */
@Slf4j
@Component
@Profile({"dev", "postgres"})
public class RestClientDemo {

    private static final String BASE_URL = "http://localhost:8080";

    private final RestTemplate restTemplate;

    public RestClientDemo() {
        this.restTemplate = new RestTemplate();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runDemo() {
        log.info("╔══════════════════════════════════════════╗");
        log.info("║     bookRoot REST CLIENT DEMO            ║");
        log.info("╚══════════════════════════════════════════╝");

        demoGetTasks();
        demoGetTasksWithFilters();
        demoPostTaskNote();
        demoPostVoteNote();
        demoGetShare();

        log.info("══════════ REST CLIENT DEMO COMPLETED ══════════");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  1. GET /api/tasks – public list of approved tasks
    // ──────────────────────────────────────────────────────────────────────────

    private void demoGetTasks() {
        String url = BASE_URL + "/api/tasks?page=0&size=5";
        log.info("──────────────────────────────────────────");
        log.info("1. GET {} (public – no auth required)", url);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            log.info("   Status: {}", response.getStatusCode());
            if (response.getBody() != null) {
                Object totalElements = response.getBody().get("totalElements");
                Object content = response.getBody().get("content");
                log.info("   Total tasks: {}", totalElements);
                log.info("   Content (page 0): {}", content);
            }
        } catch (RestClientException e) {
            log.warn("   Could not reach server: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  2. GET /api/tasks with sort and category filter
    // ──────────────────────────────────────────────────────────────────────────

    private void demoGetTasksWithFilters() {
        String url = BASE_URL + "/api/tasks?page=0&size=3&sort=createdDate,desc";
        log.info("──────────────────────────────────────────");
        log.info("2. GET {} (sorted by date DESC)", url);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("   Status: {}", response.getStatusCode());
            String body = response.getBody();
            log.info("   Body snippet: {}", body != null ? body.substring(0, Math.min(250, body.length())) : "null");
        } catch (RestClientException e) {
            log.warn("   Could not reach server: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  3. POST /api/tasks – requires authentication (informational note)
    // ──────────────────────────────────────────────────────────────────────────

    private void demoPostTaskNote() {
        log.info("──────────────────────────────────────────");
        log.info("3. POST /api/tasks (requires USER or ADMIN authentication)");
        log.info("   Example request body:");
        log.info("   {{");
        log.info("     \"title\":      \"Przykładowe zadanie z algebry\",");
        log.info("     \"content\":    \"Oblicz wyznacznik macierzy 3x3...\",");
        log.info("     \"categoryId\": 1,");
        log.info("     \"anonymous\":  false,");
        log.info("     \"taskType\":   \"OPEN\"");
        log.info("   }}");
        log.info("   Response: 201 Created + TaskDTO JSON");
        log.info("   Note: Send with session cookie or Basic Auth header.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  4. POST /api/votes – requires authentication (informational note)
    // ──────────────────────────────────────────────────────────────────────────

    private void demoPostVoteNote() {
        log.info("──────────────────────────────────────────");
        log.info("4. POST /api/votes (requires USER authentication – buffered in session)");
        log.info("   Example request body:");
        log.info("   {{");
        log.info("     \"answerId\": 1,");
        log.info("     \"voteType\": \"UPVOTE\"");
        log.info("   }}");
        log.info("   Response: 200 OK + {{ \"answerId\": 1, \"score\": 1 }}");
        log.info("   Note: Vote is stored in HttpSession and flushed to DB on logout.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  5. GET /api/shares/{token} – public
    // ──────────────────────────────────────────────────────────────────────────

    private void demoGetShare() {
        // We try to call with a non-existing token to demonstrate the endpoint structure
        String url = BASE_URL + "/api/shares/demo-token-not-exist";
        log.info("──────────────────────────────────────────");
        log.info("5. GET {} (public – fetch task by share token)", url);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("   Status: {}", response.getStatusCode());
            log.info("   Body: {}", response.getBody());
        } catch (RestClientException e) {
            // Expected: 404 or 410 if token not found – that's fine for demo
            log.info("   Expected response for non-existing token: {}", e.getMessage());
            log.info("   Valid token would return: 200 OK + TaskDTO JSON");
        }
    }
}
