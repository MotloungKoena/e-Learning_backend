package org.example.elearning_backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MaterialRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Integer duration;  // For videos

    private Integer orderIndex;
}