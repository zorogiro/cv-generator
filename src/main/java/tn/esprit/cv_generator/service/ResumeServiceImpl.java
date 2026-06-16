package tn.esprit.cv_generator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.cv_generator.dto.ResumeRequest;
import tn.esprit.cv_generator.dto.ResumeResponse;
import tn.esprit.cv_generator.entity.Education;
import tn.esprit.cv_generator.entity.Resume;
import tn.esprit.cv_generator.entity.WorkExperience;
import tn.esprit.cv_generator.repository.ResumeRepository;

import tn.esprit.cv_generator.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;

    @Override
    public ResumeResponse create(ResumeRequest request) {
        Resume resume = toEntity(request);
        return toResponse(resumeRepository.save(resume));
    }

    @Override
    public ResumeResponse findById(Long id) {
        return resumeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + id));
    }

    @Override
    public List<ResumeResponse> findAll() {
        return resumeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public ResumeResponse update(Long id, ResumeRequest request) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + id));
        applyRequest(resume, request);
        return toResponse(resumeRepository.save(resume));
    }

    @Override
    public void delete(Long id) {
        resumeRepository.deleteById(id);
    }

    private Resume toEntity(ResumeRequest req) {
        Resume resume = Resume.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .location(req.getLocation())
                .linkedInUrl(req.getLinkedInUrl())
                .githubUrl(req.getGithubUrl())
                .summary(req.getSummary())
                .skills(req.getSkills() != null ? new ArrayList<>(req.getSkills()) : new ArrayList<>())
                .build();
        applyChildren(resume, req);
        return resume;
    }

    private void applyRequest(Resume resume, ResumeRequest req) {
        resume.setFullName(req.getFullName());
        resume.setEmail(req.getEmail());
        resume.setPhone(req.getPhone());
        resume.setLocation(req.getLocation());
        resume.setLinkedInUrl(req.getLinkedInUrl());
        resume.setGithubUrl(req.getGithubUrl());
        resume.setSummary(req.getSummary());
        resume.setSkills(req.getSkills() != null ? new ArrayList<>(req.getSkills()) : new ArrayList<>());
        resume.getWorkExperiences().clear();
        resume.getEducations().clear();
        applyChildren(resume, req);
    }

    private void applyChildren(Resume resume, ResumeRequest req) {
        if (req.getWorkExperiences() != null) {
            req.getWorkExperiences().forEach(we -> resume.getWorkExperiences().add(
                    WorkExperience.builder()
                            .company(we.getCompany())
                            .title(we.getTitle())
                            .location(we.getLocation())
                            .startDate(we.getStartDate())
                            .endDate(we.getEndDate())
                            .description(we.getDescription())
                            .resume(resume)
                            .build()));
        }
        if (req.getEducations() != null) {
            req.getEducations().forEach(edu -> resume.getEducations().add(
                    Education.builder()
                            .institution(edu.getInstitution())
                            .degree(edu.getDegree())
                            .fieldOfStudy(edu.getFieldOfStudy())
                            .startDate(edu.getStartDate())
                            .endDate(edu.getEndDate())
                            .resume(resume)
                            .build()));
        }
    }

    private ResumeResponse toResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .fullName(resume.getFullName())
                .email(resume.getEmail())
                .phone(resume.getPhone())
                .location(resume.getLocation())
                .linkedInUrl(resume.getLinkedInUrl())
                .githubUrl(resume.getGithubUrl())
                .summary(resume.getSummary())
                .skills(resume.getSkills())
                .workExperiences(resume.getWorkExperiences().stream()
                        .map(we -> ResumeResponse.WorkExperienceDto.builder()
                                .id(we.getId())
                                .company(we.getCompany())
                                .title(we.getTitle())
                                .location(we.getLocation())
                                .startDate(we.getStartDate())
                                .endDate(we.getEndDate())
                                .description(we.getDescription())
                                .build())
                        .toList())
                .educations(resume.getEducations().stream()
                        .map(edu -> ResumeResponse.EducationDto.builder()
                                .id(edu.getId())
                                .institution(edu.getInstitution())
                                .degree(edu.getDegree())
                                .fieldOfStudy(edu.getFieldOfStudy())
                                .startDate(edu.getStartDate())
                                .endDate(edu.getEndDate())
                                .build())
                        .toList())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }
}
