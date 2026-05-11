package com.green.core.application.course;

import com.green.common.model.ResultResponse;
import com.green.core.application.course.model.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/student/courses")
public class CourseController {

    private final CourseService courseService;

    // API-ENRL-06: 수강 신청 페이지 활성화 제어
    @GetMapping("/status")
    public ResultResponse<CourseStatusRes> getCourseStatus() {
        CourseStatusRes res = courseService.getCourseStatus();
        return ResultResponse.<CourseStatusRes>builder()
                .message("수강 신청 상태 조회")
                .data(res)
                .build();
    }

    // API-ENRL-01: 수강 가능 강의 전체 조회
    @GetMapping
    public ResultResponse<List<CourseRes>> getCourses() {
        List<CourseRes> list = courseService.getCourses();
        return ResultResponse.<List<CourseRes>>builder()
                .message("수강 가능 강의 목록 조회")
                .data(list)
                .build();
    }

    // API-ENRL-02: 내 수강 신청 목록 조회
    @GetMapping("/my")
    public ResultResponse<MyCourseListRes> getMyCourses() {
        MyCourseListRes list = courseService.getMyCourses();
        return ResultResponse.<MyCourseListRes>builder()
                .message("내 수강 신청 목록 조회")
                .data(list)
                .build();
    }

    // API-ENRL-03: 수강 신청 실행
    @PostMapping
    public ResultResponse<CourseCreateRes> createCourse(@Valid @RequestBody CourseCreateReq req) {
        CourseCreateRes res = courseService.createCourse(req);
        return ResultResponse.<CourseCreateRes>builder()
                .message("수강 신청 완료")
                .data(res)
                .build();
    }

    // API-ENRL-04: 수강 신청 취소
    @DeleteMapping("/{lectureId}")
    public ResultResponse<Void> deleteCourse(@PathVariable Long lectureId) {
        courseService.deleteCourse(lectureId);
        return ResultResponse.<Void>builder()
                .message("수강 신청 취소 완료")
                .build();
    }

    // 임시 - lecture 담당자 res 완성 전까지 테스트용
    @GetMapping("/lectures")
    public ResultResponse<List<CourseLectureRes>> getLectures() {
        List<CourseLectureRes> list = courseService.getLectures();
        return ResultResponse.<List<CourseLectureRes>>builder()
                .message("강의 목록 조회 (임시)")
                .data(list)
                .build();
    }
}