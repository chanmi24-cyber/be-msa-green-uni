package com.green.core.application.grade;

import com.green.common.model.ResultResponse;
import com.green.core.application.grade.model.GradeStudentRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// API-GPA-05: 학생 성적 조회
@RestController
@RequiredArgsConstructor
@RequestMapping("/student/grades")
public class StudentGradeController {

    private final GradeService gradeService;

    @GetMapping("/my")
    public ResponseEntity<ResultResponse<GradeStudentRes>> getStudentGrades(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester) {
        return ResponseEntity.ok(
                new ResultResponse<>("성적 조회 성공", gradeService.getStudentGrades(year, semester)));
    }
}