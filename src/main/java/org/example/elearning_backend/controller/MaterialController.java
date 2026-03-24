package org.example.elearning_backend.controller;

import org.example.elearning_backend.dto.MaterialRequest;
import org.example.elearning_backend.model.Material;
import org.example.elearning_backend.security.UserDetailsImpl;
import org.example.elearning_backend.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")  // FIXED
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @PostMapping(value = "/courses/{courseId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> uploadMaterial(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "orderIndex", required = false) Integer orderIndex,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }

            // Upload file and create material
            Material material = materialService.uploadMaterial(
                    courseId, file, title, description, duration, orderIndex, currentUser.getId());

            return ResponseEntity.ok(material);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/materials")
    public ResponseEntity<?> getCourseMaterials(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            List<Material> materials = materialService.getCourseMaterials(
                    courseId, currentUser.getId(), currentUser.getAuthorities().toString());
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/materials/{materialId}")
    public ResponseEntity<?> getMaterial(
            @PathVariable Long materialId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            Material material = materialService.getMaterial(
                    materialId, currentUser.getId(), currentUser.getAuthorities().toString());
            return ResponseEntity.ok(material);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/materials/{materialId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> deleteMaterial(
            @PathVariable Long materialId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            materialService.deleteMaterial(materialId, currentUser.getId());
            return ResponseEntity.ok("Material deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not delete file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/materials/{materialId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<?> updateMaterial(
            @PathVariable Long materialId,
            @RequestBody MaterialRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        try {
            Material material = materialService.updateMaterial(
                    materialId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getDuration(),
                    request.getOrderIndex(),
                    currentUser.getId());

            return ResponseEntity.ok(material);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
}