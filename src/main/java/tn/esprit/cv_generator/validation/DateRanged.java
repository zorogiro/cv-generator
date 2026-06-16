package tn.esprit.cv_generator.validation;

import java.time.LocalDate;

public interface DateRanged {
    LocalDate getStartDate();
    LocalDate getEndDate();
}
