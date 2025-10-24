package io.github.asyncbtd.sap.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TestController {

    @PreAuthorize("hasAuthority('IMAGE_DELETE')")
    @GetMapping("/IMAGE_DELETE")
    public ResponseEntity<?> imageDelete() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("hasAuthority('IMAGE_WRITE')")
    @PostMapping("/IMAGE_WRITE")
    public ResponseEntity<?> imageWrite() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
