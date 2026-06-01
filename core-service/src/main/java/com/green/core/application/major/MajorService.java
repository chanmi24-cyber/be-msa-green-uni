package com.green.core.application.major;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumBuilding;
import com.green.common.enumcode.EnumChangeType;
import com.green.common.exception.BusinessException;
import com.green.common.kafka.MajorEvent;
import com.green.common.model.MemberDto;
import com.green.common.outbox.Outbox;
import com.green.common.outbox.OutboxRepository;
import com.green.core.application.major.model.*;
import com.green.core.entity.major.College;
import com.green.core.entity.major.Major;
import com.green.core.entity.major.MajorHistory;
import com.green.core.enumcode.EnumMajorStatus;
import com.green.core.exception.MajorErrorCode;
import com.green.core.repository.ProfessorCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorService {
    private final MajorRepository majorRepository;
    private final CollegeRepository collegeRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final MajorHistoryRepository majorHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;


    //유효성 검사
    private void validateRequiredFields(MajorCreateUpdateReq req) {
        // 1. 학과명 (공백 제외)
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new BusinessException(MajorErrorCode.MAJOR_NAME_REQUIRED);
        }
        // 2. 소속대학 ID
        if (req.getCollegeId() == null) {
            throw new BusinessException(MajorErrorCode.COLLEGE_ID_REQUIRED);
        }
        // 3. 건물 선택 (EnumBuilding)
        if (req.getMajorBuilding() == null) {
            throw new BusinessException(MajorErrorCode.BUILDING_REQUIRED);
        }
        // 4. 호수 (공백 제외)
        if (req.getRoom() == null || req.getRoom().trim().isEmpty()) {
            throw new BusinessException(MajorErrorCode.ROOM_REQUIRED);
        }
        // 5. 전화번호 (공백 제외)
        if (req.getTel() == null || req.getTel().trim().isEmpty()) {
            throw new BusinessException(MajorErrorCode.TEL_REQUIRED);
        }
        // 6. 수업연한
        if (req.getCourseDuration() == null || req.getCourseDuration() < 4) {
            throw new BusinessException(MajorErrorCode.COURSE_DURATION_INVALID);
        }
        // 7. 입학정원
        if (req.getCapacity() == null || req.getCapacity() < 30) {
            throw new BusinessException(MajorErrorCode.CAPACITY_INVALID);
        }
        // 8. 개설일
        if (req.getFoundedDate() == null) {
            throw new BusinessException(MajorErrorCode.FOUNDED_DATE_REQUIRED);
        }
    }

    // API-DEPT-01: 학과 개설
    @Transactional
    public Long createMajor(MajorCreateUpdateReq req) {
        validateRequiredFields(req);
        College college = collegeRepository.findById(req.getCollegeId())
                .orElseThrow(() -> new BusinessException(MajorErrorCode.COLLEGE_NOT_FOUND));
        log.info("1. college 조회 완료: {}", college.getName());

        validateDuplicate(req.getName(), req.getMajorBuilding(), req.getRoom(),
                req.getTel(), req.getChairProfessorCode(), null);
        log.info("2. 중복 검증 완료");

        Major major = Major.builder()
                .name(req.getName())
                .active(req.getActive())
                .college(college)
                .majorBuilding(req.getMajorBuilding())
                .room(req.getRoom())
                .tel(req.getTel())
                .capacity(req.getCapacity())
                .professorCode(req.getChairProfessorCode())
                .info(req.getInfo())
                .courseDuration(req.getCourseDuration())
                .foundedDate(req.getFoundedDate())
                .build();

        majorRepository.save(major);
        log.info("3. major 저장 완료: {}", major.getMajorId());

        MajorEvent event = MajorEvent.builder()
                .majorId(major.getMajorId())
                .name(major.getName())
                .collegeId(college.getCollegeId())
                .collegeName(college.getName())
                .active(major.getActive().name())
                .eventType(EventType.E_CREATED)
                .build();

        saveToOutbox(event);
        log.info("4. outbox 저장 완료");

        return major.getMajorId();
    }

    // API-DEPT-01: 학과 수정
    @Transactional
    public void editMajor(MemberDto memberDto, Long majorId, MajorCreateUpdateReq req) {
        validateRequiredFields(req);
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new BusinessException(MajorErrorCode.MAJOR_NOT_FOUND));

        // [추가] 변경하려는 상태가 '폐지(CLOSED)'인 경우 학과 내 학생 존재 여부 검증
        if (req.getActive() == EnumMajorStatus.CLOSED) {
            boolean hasStudents = majorRepository.existsStudentsInMajor(majorId);
            if (hasStudents) {
                throw new BusinessException(MajorErrorCode.HAS_ACTIVE_STUDENTS);
            }
        }

        validateDuplicate(req.getName(), req.getMajorBuilding(), req.getRoom(),
                req.getTel(), req.getChairProfessorCode(), majorId);

        College college = collegeRepository.findById(req.getCollegeId())
                .orElseThrow(() -> new BusinessException(MajorErrorCode.COLLEGE_NOT_FOUND));

        major.update(req.getName(), req.getActive(), college,
                req.getMajorBuilding(), req.getRoom(), req.getTel(),
                req.getCapacity(), req.getChairProfessorCode(), req.getInfo(), req.getCourseDuration(), req.getFoundedDate(), req.getClosedDate());

        // 이벤트 발행 추가
        MajorEvent event = MajorEvent.builder()
                .majorId(major.getMajorId())
                .name(req.getName())
                .collegeId(college.getCollegeId())
                .collegeName(college.getName())
                .active(major.getActive().name())
                .eventType(EventType.E_UPDATED)
                .build();
        saveToOutbox(event);

        // 히스토리저장용 - 수정 전 데이터 JSON으로 저장
        try {
            String beforeData = String.format(
                    "{\"lectureId\":%d,\"lectureName\":\"%s\",\"active\":\"%s\"}",
                    major.getMajorId(),
                    major.getName(),
                    major.getActive()
            );

            MajorHistory history = MajorHistory.builder()
                    .major(major)
                    .changeType(EnumChangeType.UPDATE)
                    .beforeData(beforeData)
                    .changeReason("학과 수정")
                    .updatorCode(memberDto.memberCode())
                    .build();
            majorHistoryRepository.save(history);
        } catch (Exception e) {
            throw new RuntimeException("히스토리 저장 실패", e);
        }
    }

    // API-DEPT-03: 관리자 전체 목록 조회
    public Page<MajorRes> getMajorList(String status, String search, Pageable pageable) {
        return majorRepository.findMajorListWithFilter(status, search, pageable)
                .map(row -> MajorRes.builder()
                        .majorId(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .majorBuilding(EnumBuilding.valueOf((String) row[2]))
                        .room((String) row[3])
                        .tel((String) row[4])
                        .professorCode(row[5] != null ? ((Number) row[5]).longValue() : null)
                        .capacity(((Number) row[6]).intValue())
                        .collegeId(((Number) row[7]).longValue())
                        .active(EnumMajorStatus.valueOf((String) row[8]))
                        .professorCount(((Number) row[9]).intValue())
                        .build());
    }

    // API-DEPT-04: 일반 학과 목록 조회
    public List<MajorListRes> getMajorSimpleList() {
        return majorRepository.findByActiveNot(EnumMajorStatus.CLOSED).stream()
                .map(m -> MajorListRes.builder()
                        .majorId(m.getMajorId())
                        .majorName(m.getName())
                        .build())
                .toList();
    }

    // API-DEPT-05: 학과 상세 조회
    public MajorDetailRes getMajor(Long majorId) {
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new BusinessException(MajorErrorCode.MAJOR_NOT_FOUND));

        return MajorDetailRes.builder()
                .majorId(major.getMajorId())
                .name(major.getName())
                .active(major.getActive())
                .college(major.getCollege().getName())
                .majorBuilding(major.getMajorBuilding())
                .room(major.getRoom())
                .tel(major.getTel())
                .professorCode(major.getProfessorCode())
                .capacity(major.getCapacity())
                .info(major.getInfo())
                .courseDuration(major.getCourseDuration())
                .foundedDate(major.getFoundedDate())
                .closedDate(major.getClosedDate())
                .build();
    }

    // 중복 검증 공통 로직
    private void validateDuplicate(String name, EnumBuilding majorBuilding, String room,
                                   String tel, Long professorCode, Long majorId) {
        boolean nameDup = majorId == null
                ? majorRepository.existsByName(name)
                : majorRepository.existsByNameAndMajorIdNot(name, majorId);
        if (nameDup) throw new BusinessException(MajorErrorCode.MAJOR_NAME_DUPLICATED);

        boolean telDup = majorId == null
                ? majorRepository.existsByTel(tel)
                : majorRepository.existsByTelAndMajorIdNot(tel, majorId);
        if (telDup) throw new BusinessException(MajorErrorCode.TEL_DUPLICATED);

        if (professorCode != null) {
            boolean profDup = majorId == null
                    ? majorRepository.existsByProfessorCode(professorCode)
                    : majorRepository.existsByProfessorCodeAndMajorIdNot(professorCode, majorId);
            if (profDup) throw new BusinessException(MajorErrorCode.CHAIR_PROFESSOR_DUPLICATED);
        }

        boolean buildingRoomDup = majorId == null
                ? majorRepository.existsByMajorBuildingAndRoom(majorBuilding, room)
                : majorRepository.existsByMajorBuildingAndRoomAndMajorIdNot(majorBuilding, room, majorId);
        if (buildingRoomDup) throw new BusinessException(MajorErrorCode.OFFICE_ROOM_DUPLICATED);
    }

    private void saveToOutbox(MajorEvent event) {
        try {
            log.info("saveToOutbox 시작 - majorId: {}", event.getMajorId());
            String payload = objectMapper.writeValueAsString(event);
            log.info("payload: {}", payload);
            Outbox outbox = Outbox.builder()
                    .topic("major-events")
                    .aggregateId(event.getMajorId())
                    .eventType(event.getEventType().name())
                    .payload(payload)
                    .build();
            outboxRepository.save(outbox);
            outboxRepository.flush(); // 추가
            log.info("Outbox ID: {}", outbox.getId()); // ID가 찍히는지 확인
            log.info("outbox 저장 완료");
        } catch (JsonProcessingException e) {
            log.error("Outbox 직렬화 실패", e);
            throw new RuntimeException("Outbox 직렬화 실패", e);
        } catch (Exception e) {
            log.error("Outbox 저장 중 알 수 없는 오류", e); // 추가 - 이게 핵심
            throw e;
        }
    }

    public List<CollegeListRes> getCollegeList() {
        return collegeRepository.findAll().stream()
                .map(c -> CollegeListRes.builder()
                        .collegeId(c.getCollegeId())
                        .name(c.getName())
                        .build())
                .toList();
    }

    private final ProfessorCacheRepository professorCacheRepository;

    // API-DEPT-02: 교수 목록 조회(캐시테이블)
    public List<ProfessorListRes> getProfessorList() {
        return professorCacheRepository.findAll().stream()
                .map(p -> ProfessorListRes.builder()
                        .memberCode(p.getMemberCode())
                        .name(p.getName())
                        .build())
                .toList();
    }

    //빌딩 목록 조회
    public List<BuildingRes> getBuildingList() {
        return Arrays.stream(EnumBuilding.values())
                .map(b -> BuildingRes.builder()
                        .code(b.name())
                        .name(b.getValue())
                        .build())
                .toList();
    }

    public boolean hasStudents(Long majorId) {
        return majorRepository.existsStudentsInMajor(majorId);
    }
}