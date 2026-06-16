package tn.esprit.cv_generator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.cv_generator.validation.DateRanged;
import tn.esprit.cv_generator.validation.ValidDateRange;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String location;

    @Size(max = 255)
    private String linkedInUrl;

    @Size(max = 255)
    private String githubUrl;

    @Size(max = 2000)
    private String summary;

    private List<String> skills;

    @Valid
    private List<WorkExperienceDto> workExperiences;

    @Valid
    private List<EducationDto> educations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ValidDateRange
    public static class WorkExperienceDto implements DateRanged {

        @NotBlank
        private String company;

        @NotBlank
        private String title;

        @Size(max = 100)
        private String location;

        @NotNull
        @PastOrPresent
        private LocalDate startDate;

        @PastOrPresent
        private LocalDate endDate;

        @Size(max = 2000)
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ValidDateRange
    public static class EducationDto implements DateRanged {

        @NotBlank
        private String institution;

        @NotBlank
        private String degree;

        @Size(max = 100)
        private String fieldOfStudy;

        @NotNull
        @PastOrPresent
        private LocalDate startDate;

        @PastOrPresent
        private LocalDate endDate;
    }
}
