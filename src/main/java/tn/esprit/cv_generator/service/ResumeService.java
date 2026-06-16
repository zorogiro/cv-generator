package tn.esprit.cv_generator.service;

import tn.esprit.cv_generator.dto.ResumeRequest;
import tn.esprit.cv_generator.dto.ResumeResponse;
import tn.esprit.cv_generator.entity.User;

import java.util.List;

public interface ResumeService {

    ResumeResponse create(ResumeRequest request, User user);

    ResumeResponse findByIdAndUser(Long id, User user);

    List<ResumeResponse> findAllByUser(User user);

    ResumeResponse update(Long id, ResumeRequest request, User user);

    void delete(Long id, User user);
}
