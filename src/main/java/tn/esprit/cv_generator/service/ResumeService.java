package tn.esprit.cv_generator.service;

import tn.esprit.cv_generator.dto.ResumeRequest;
import tn.esprit.cv_generator.dto.ResumeResponse;

import java.util.List;

public interface ResumeService {

    ResumeResponse create(ResumeRequest request);

    ResumeResponse findById(Long id);

    List<ResumeResponse> findAll();

    ResumeResponse update(Long id, ResumeRequest request);

    void delete(Long id);
}
