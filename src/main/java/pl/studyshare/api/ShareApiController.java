package pl.studyshare.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.studyshare.dto.ShareCreateRequest;
import pl.studyshare.dto.ShareTokenDTO;
import pl.studyshare.service.ShareService;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
public class ShareApiController {

    private final ShareService shareService;

    @PostMapping
    public ResponseEntity<ShareTokenDTO> createShareToken(@RequestBody @Valid ShareCreateRequest request,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        ShareTokenDTO response = shareService.createShareToken(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
