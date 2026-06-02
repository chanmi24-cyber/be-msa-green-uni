package com.green.core.application.course;

import com.green.common.auth.MemberContext;
import com.green.common.enumcode.EnumScheduleType;
import com.green.common.enumcode.EnumStudentStatus;
import com.green.common.model.MemberDto;
import com.green.core.entity.cache.StudentCache;
import com.green.core.repository.ScheduleCacheRepository;
import com.green.core.repository.StudentCacheRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentStatusInterceptor implements HandlerInterceptor {

    private final StudentCacheRepository studentCacheRepository;
    private final ScheduleCacheRepository scheduleCacheRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 학생 상태 확인
        MemberDto member = MemberContext.get();
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
        }

        StudentCache student = studentCacheRepository.findById(member.memberCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "학생 정보를 찾을 수 없습니다."));

        if (student.getStatus() != EnumStudentStatus.ENROLLED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "재학 중인 학생만 수강 신청 페이지에 접근할 수 있습니다.");
        }
        log.info("학생 상태 확인 완료 - memberCode: {}, status: {}", member.memberCode(), student.getStatus());

        // 2. 수강 신청 기간 확인
        boolean isRegistrationOpen = !scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_REGISTRATION).isEmpty();
        boolean isModificationOpen = !scheduleCacheRepository
                .findByTypeAndIsActiveTrue(EnumScheduleType.COURSE_MODIFICATION).isEmpty();

        if (!isRegistrationOpen && !isModificationOpen) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수강 신청 기간이 아닙니다.");
        }
        log.info("수강 신청 기간 확인 완료 - 수강신청: {}, 수강정정: {}", isRegistrationOpen, isModificationOpen);

        return true;
    }
}
