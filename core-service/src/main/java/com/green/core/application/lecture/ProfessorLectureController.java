package com.green.core.application.lecture;

import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumBuilding;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.core.application.lecture.model.*;
import com.green.core.application.lecture.repository.ClassroomRepository;
import com.green.core.entity.lecture.Classroom;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/professor/lectures")
public class ProfessorLectureController {
    private final LectureService lectureService;
    private final ClassroomRepository classroomRepository;

    @PostMapping
    public ResultResponse<?> createLecture(@Valid @RequestBody LectureCreateReq req){
        MemberDto memberDto = MemberContext.get();
        lectureService.createLecture(memberDto, req);
        return ResultResponse.builder()
                .message("강의 개설 성공")
                .build();
    }

    @GetMapping("/my")
    public ResultResponse<List<MyLectureListRes>> getProfessorMyLectures(@ModelAttribute MyLectureListReq req) {
        MemberDto memberDto = MemberContext.get();
        return ResultResponse.<List<MyLectureListRes>>builder()
                .message("내 강의 목록 조회 성공")
                .data(lectureService.getProfessorMyLectures(memberDto, req))
                .build();
    }

    @PatchMapping("/{lectureId}")
    public ResultResponse<?> updateLecture(
            @PathVariable Long lectureId,
            @RequestBody LectureDetailReq req) {
        MemberDto memberDto = MemberContext.get();
        lectureService.updateLecture(memberDto, lectureId, req);
        return ResultResponse.builder()
                .message("강의 수정 성공")
                .build();
    }

    @DeleteMapping("/{lectureId}")
    public ResultResponse<?> deleteLecture(@PathVariable Long lectureId) {
        MemberDto memberDto = MemberContext.get();
        lectureService.deleteLecture(memberDto, lectureId);
        return ResultResponse.builder()
                .message("강의 삭제 성공")
                .build();
    }

    @GetMapping("/classrooms")
    public ResultResponse<?> getClassrooms(@RequestParam EnumBuilding building) {
        List<Classroom> rooms = classroomRepository.findByBuilding(building);
        return ResultResponse.builder()
                .message("강의실 목록 조회")
                .data(rooms)
                .build();
    }



}