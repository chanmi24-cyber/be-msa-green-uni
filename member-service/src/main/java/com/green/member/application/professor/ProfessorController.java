package com.green.member.application.professor;

import com.green.common.auth.MemberContext;
import com.green.common.model.MemberDto;
import com.green.common.model.ResultResponse;
import com.green.member.application.professor.model.ProfessorHistoryRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/professor")
public class ProfessorController {
    private final ProfessorService professorService;

    @GetMapping("/history")
    public ResultResponse<?> findHistory(){
        MemberDto loginMember = MemberContext.get();
        List<ProfessorHistoryRes> res = professorService.getStatusHistory( loginMember.memberCode() );
        return ResultResponse.builder()
                .message("교수 상태 변경 이력 조회")
                .data(res)
                .build();
    }
}
