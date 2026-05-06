package com.green.member.application.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.common.kafka.StudentEvent;
import com.green.common.outbox.Outbox;
import com.green.common.outbox.OutboxRepository;
import com.green.member.application.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final StudentRepository studentRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public MemberCreateRes createMember(MemberCreateReq req, MultipartFile mf){
        MemberCreateRes res = new MemberCreateRes(); // 싱글톤이 아닌 메서드 안에서 작동

        //파일 업로드가 되었으면 저장하는 파일명을 테이블에 저장
        String savedPicFileName = mf == null ? null : myFileUtil.makeRandomFileName(mf);
        req.setPic(savedPicFileName);

        // member table insert
        memberMapper.createMember(req);

        // 프로파일 이미지 저장
        if( mf != null ){ //이미지가 업로드 되었다면
            long id = req.getMemberId();
            String middlePath = "member/" + id;
            myFileUtil.makeFolders(middlePath); // 디렉토리 생성
            String fullFilePath = String.format("%s/%s", middlePath, savedPicFileName);
            try{
                myFileUtil.transferTo(mf, fullFilePath);
            }catch (IOException e){
                // 파일 저장 실패시 pic을 null로 되돌리기
                req.setPic(null);
                memberMapper.updateMemberPic(req);  // pic null로 업데이트
                log.error("파일 저장 실패: {}", e.getMessage());
            }
        }

        // 멤버코드: 입학연도(4자리) + 구분코드(1자리) + 순번(3자리)

        // 입학연도
        String entryYear = req.getEntryDate().substring(0,4);

        // role에 따른 구분코드
        String roleNum;
        switch (req.getRole()){
            case "admin"     -> roleNum = "3";
            case "professor" -> roleNum = "2";
            default          -> roleNum = "1";
        }

        // 멤버코드 생성
        String code = entryYear + roleNum + String.format("%03d", req.getMemberId());
        req.setCode(code);

        // 생일을 초기 비밀번호
        String rawPw = req.getBirth().replace("-", ""); //- 제거
        String hashedPw = passwordEncoder.encode(rawPw);
        req.setPassword(hashedPw);

        // 멤버코드와 비밀번호 삽입
        memberMapper.updateMemberCodeAPw(req);

        switch (req.getRole()){
            case "admin"     -> memberMapper.createStaff(req);
            case "professor" -> memberMapper.createProfessor(req);
            default          -> memberMapper.createStudent(req);
        }

        res.setMemberCode(req.getCode());
        res.setMemberRole(req.getRole());
        return res;
    }



//
//    public void test(StudentCreateReq req) {
//
//        Student newStudent = new Student();
//        newStudent.setName( req.getName() );
//
//        studentRepository.save( newStudent );
//
//        StudentEvent studentEvent = StudentEvent.builder()
//                .memberCode(newStudent.getMemberCode() )
//                .name( newStudent.getName() )
//                .eventType( EventType.E_CREATED )
//                .build();
//
//        saveToOutbox(studentEvent);
//    }

    private void saveToOutbox(StudentEvent studentEvent) {
        try {
            String payload = objectMapper.writeValueAsString(studentEvent);
            Outbox outbox = Outbox.builder()
                    .topic("student-events")
                    .aggregateId( studentEvent.getMemberCode() )
                    .eventType( studentEvent.getEventType().name() )
                    .payload( payload )
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Outbox 직렬화 실패", e);
        }

    }
}
