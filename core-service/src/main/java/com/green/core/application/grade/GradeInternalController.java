package com.green.core.application.grade;

import com.green.common.client.GpaResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping("/internal/grades")
@RequiredArgsConstructor
public class GradeInternalController {
    private final GradeRepository gradeRepository;

    @GetMapping("/gpa/{studentCode}")
    public GpaResult getGpa(@PathVariable Long studentCode) {
        Double rawGpa = gradeRepository.calcWeightedGpaByStudentCode(studentCode);
        BigDecimal gpa = (rawGpa != null)
                ? BigDecimal.valueOf(rawGpa).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        int totalCredits = gradeRepository.sumTotalCreditsByStudentCode(studentCode);
        return new GpaResult(gpa, totalCredits);
    }
}
