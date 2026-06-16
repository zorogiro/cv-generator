package tn.esprit.cv_generator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.cv_generator.entity.Resume;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
