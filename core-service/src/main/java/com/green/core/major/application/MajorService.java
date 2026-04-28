package com.green.core.major.application;
import com.green.core.major.application.model.MajorCreateReq;
import com.green.core.major.entity.Major;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;

//    @Transactional
//    public void createMajor(MajorCreateReq req) {
//        Major major = new Major();
//
//        // TSID 라이브러리 사용 시 별도 생성 로직 필요 (현재는 수동 할당 가정)
//        // major.setMajorId(tsidGenerator.nextId());
//
//        major.setMajorCode(req.getMajorCode());
//        major.setName(req.getName());
//        major.setCollegeId(1L); // 요청사항: college_id 1로 고정
//        major.setRoom(req.getRoom());
//        major.setTel(req.getTel());
//        major.setProfessorCode(req.getProfessorCode());
//        major.setCapacity(req.getCapacity());
//
//        if (req.getActive() != null) {
//            major.setActive(req.getActive());
//        }
//
//        majorRepository.save(major);
//    }
}