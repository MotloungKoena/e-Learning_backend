package org.example.elearning_backend.controller;

import org.example.elearning_backend.security.UserDetailsImpl;
import org.example.elearning_backend.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @GetMapping("/download/{enrollmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            byte[] pdfBytes = certificateService.generateCertificate(enrollmentId, currentUser.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "certificate.pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}