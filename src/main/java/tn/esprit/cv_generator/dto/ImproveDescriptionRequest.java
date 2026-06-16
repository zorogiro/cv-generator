package tn.esprit.cv_generator.dto;

import jakarta.validation.constraints.NotBlank;

public record ImproveDescriptionRequest(@NotBlank String description) {}
