package pl.studyshare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import pl.studyshare.dto.SortPreferences;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieService {

    private final ObjectMapper objectMapper;
    private static final String COOKIE_NAME = "sortPrefs";

    /**
     * Reads the sort preferences from the cookie in the request.
     *
     * @param request the HTTP request
     * @return Optional containing SortPreferences if cookie exists and is valid, empty otherwise
     */
    public Optional<SortPreferences> readSortPreferences(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .flatMap(cookie -> {
                    try {
                        String decodedJson = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                        SortPreferences prefs = objectMapper.readValue(decodedJson, SortPreferences.class);
                        return Optional.of(prefs);
                    } catch (Exception e) {
                        log.error("Failed to parse sortPrefs cookie: {}", e.getMessage());
                        return Optional.empty();
                    }
                });
    }

    /**
     * Writes the sort preferences to the response cookie.
     *
     * @param response the HTTP response
     * @param prefs the sort preferences to save
     */
    public void writeSortPreferences(HttpServletResponse response, SortPreferences prefs) {
        try {
            String json = objectMapper.writeValueAsString(prefs);
            String encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8);

            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, encodedJson)
                    .maxAge(Duration.ofDays(30))
                    .httpOnly(true)
                    .sameSite("Lax")
                    .path("/")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        } catch (Exception e) {
            log.error("Failed to write sortPrefs cookie: {}", e.getMessage());
        }
    }
}
