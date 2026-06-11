package pl.studyshare.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import pl.studyshare.dto.TaskCreateRequest;

@Slf4j
public class RestClientDemo {

    public static void main(String[] args) {
        log.info("=== STARTING REST CLIENT DEMO ===");
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "http://localhost:8080/api/tasks";

        try {
            log.info("Calling GET {}...", baseUrl);
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);
            log.info("GET Response Code: {}", response.getStatusCode());
            log.info("GET Response Body snippet: {}", 
                    response.getBody() != null ? response.getBody().substring(0, Math.min(300, response.getBody().length())) : "null");

            log.info("To perform POST /api/tasks or other protected requests, configure basic authentication or pass session cookies in the headers.");
        } catch (Exception e) {
            log.error("Failed to execute REST Client Demo. Make sure the application is running on port 8080: {}", e.getMessage());
        }
    }
}
