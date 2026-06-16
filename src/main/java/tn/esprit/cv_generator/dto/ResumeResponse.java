package tn.esprit.cv_generator.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String linkedInUrl;
    private String githubUrl;
    private String summary;
    private List<String> skills;
    private List<WorkExperienceDto> workExperiences;
    private List<EducationDto> educations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkExperienceDto {
        private Long id;
        private String company;
        private String title;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationDto {
        private Long id;
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
