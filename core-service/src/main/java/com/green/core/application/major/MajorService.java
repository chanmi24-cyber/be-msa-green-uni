package com.green.core.application.major;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.constants.EventType;
import com.green.common.enumcode.EnumBuilding;
import com.green.common.kafka.MajorEvent;
import com.green.common.outbox.Outbox;
import com.green.common.outbox.OutboxRepository;
import com.green.core.application.major.model.*;
import com.green.core.entity.major.College;
import com.green.core.entity.major.Major;
import com.green.core.enumcode.EnumMajorStatus;
import com.green.core.repository.ProfessorCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorService {
    private final MajorRepository majorRepository;
    private final CollegeRepository collegeRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    // API-DEPT-01: 학과 개설
    @Transactional
    public Long createMajor(MajorCreateUpdateReq req) {
        log.info("1. createMajor 시작");

        College college = collegeRepository.findById(req.getCollegeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 단과대입니다."));
        log.info("2. college 조회 완료: {}", college.getName());

        validateDuplicate(req.getName(), req.getMajorBuilding(), req.getRoom(),
                req.getTel(), req.getChairProfessorCode(), null);
        log.info("3. 중복 검증 완료");

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
                .build();

        majorRepository.save(major);
        log.info("4. major 저장 완료: {}", major.getMajorId());

        MajorEvent event = MajorEvent.builder()
                .majorId(major.getMajorId())
                .name(major.getName())
                .collegeName(college.getName())
                .eventType(EventType.E_CREATED)
                .build();

        saveToOutbox(event);
        log.info("5. outbox 저장 완료");

        return major.getMajorId();
    }

    // API-DEPT-01: 학과 수정
    @Transactional
    public void editMajor(Long majorId, MajorCreateUpdateReq req) {
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 학과입니다."));

        validateDuplicate(req.getName(), req.getMajorBuilding(), req.getRoom(),
                req.getTel(), req.getChairProfessorCode(), majorId);

        College college = collegeRepository.findById(req.getCollegeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 단과대입니다."));

        major.update(req.getName(), req.getActive(), college,
                req.getMajorBuilding(), req.getRoom(), req.getTel(),
                req.getCapacity(), req.getChairProfessorCode(), req.getInfo());

        // 이벤트 발행 추가
        MajorEvent event = MajorEvent.builder()
                .majorId(major.getMajorId())
                .name(req.getName())
                .collegeName(college.getName())
                .eventType(EventType.E_UPDATED)
                .build();
        saveToOutbox(event);
    }

    // API-DEPT-03: 관리자 전체 목록 조회
    public List<MajorRes> getMajorList() {
        return majorRepository.findAll().stream()
                .map(m -> MajorRes.builder()
                        .majorId(m.getMajorId())
                        .name(m.getName())
                        .majorBuilding(m.getMajorBuilding())
                        .room(m.getRoom())
                        .tel(m.getTel())
                        .professorCode(m.getProfessorCode())
                        .capacity(m.getCapacity())
                        .collegeId(m.getCollege().getCollegeId())
                        .active(m.getActive())
                        .build())
                .toList();
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 학과입니다."));

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
                .build();
    }

    // 중복 검증 공통 로직
    private void validateDuplicate(String name, EnumBuilding majorBuilding, String room,
                                   String tel, Long professorCode, Long majorId) {
        boolean nameDup = majorId == null
                ? majorRepository.existsByName(name)
                : majorRepository.existsByNameAndMajorIdNot(name, majorId);
        if (nameDup) throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 학과명입니다.");

        boolean telDup = majorId == null
                ? majorRepository.existsByTel(tel)
                : majorRepository.existsByTelAndMajorIdNot(tel, majorId);
        if (telDup) throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다.");

        if (professorCode != null) {
            boolean profDup = majorId == null
                    ? majorRepository.existsByProfessorCode(professorCode)
                    : majorRepository.existsByProfessorCodeAndMajorIdNot(professorCode, majorId);
            if (profDup) throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 다른 학과의 학과장으로 임명된 교수입니다.");
        }

        boolean buildingRoomDup = majorId == null
                ? majorRepository.existsByMajorBuildingAndRoom(majorBuilding, room)
                : majorRepository.existsByMajorBuildingAndRoomAndMajorIdNot(majorBuilding, room, majorId);
        if (buildingRoomDup) throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 학과 사무실입니다.");
    }

    private void saveToOutbox(MajorEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            Outbox outbox = Outbox.builder()
                    .topic("major-events")
                    .aggregateId(event.getMajorId())
                    .eventType(event.getEventType().name())
                    .payload(payload)
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox 직렬화 실패", e);
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
}