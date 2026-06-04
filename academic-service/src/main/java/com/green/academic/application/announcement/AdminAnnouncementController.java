package com.green.academic.application.announcement;

import com.green.academic.application.announcement.model.AnnoCreateReq;
import com.green.academic.application.announcement.model.AnnoCreateRes;
import com.green.academic.application.announcement.model.AnnoDetailRes;
import com.green.academic.application.announcement.model.AnnoUpdateReq;
import com.green.common.model.ResultResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/announcements")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    // ANNO-00 공지사항 상세 조회 (관리자)
    @GetMapping("/{annoId}")
    public ResponseEntity<ResultResponse<AnnoDetailRes>> getDetail(@PathVariable Long annoId) {
        return ResponseEntity.ok(new ResultResponse<>("공지사항 상세 조회 성공",
                announcementService.getDetail(annoId)));
    }

    // ANNO-01 공지사항 등록
    @PostMapping
    public ResponseEntity<ResultResponse<AnnoCreateRes>> create(@Valid @RequestBody AnnoCreateReq req) {
        return ResponseEntity.ok(new ResultResponse<>("공지사항 등록 성공",
                announcementService.create(req)));
    }

    // ANNO-02 공지사항 수정
    @PutMapping("/{annoId}")
    public ResponseEntity<ResultResponse<Void>> update(
            @PathVariable Long annoId,
            @Valid @RequestBody AnnoUpdateReq req) {
        announcementService.update(annoId, req);
        return ResponseEntity.ok(new ResultResponse<>("공지사항 수정 성공", null));
    }

    // ANNO-05 공지사항 삭제
    @DeleteMapping("/{annoId}")
    public ResponseEntity<ResultResponse<Void>> delete(@PathVariable Long annoId) {
        announcementService.delete(annoId);
        return ResponseEntity.ok(new ResultResponse<>("공지사항 삭제 성공", null));
    }
}