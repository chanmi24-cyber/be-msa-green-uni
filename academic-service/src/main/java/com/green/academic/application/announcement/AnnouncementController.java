package com.green.academic.application.announcement;

import com.green.academic.application.announcement.model.AnnoDetailRes;
import com.green.academic.application.announcement.model.AnnoListReq;
import com.green.academic.application.announcement.model.AnnoListRes;
import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // ANNO-03 공지사항 목록 조회 (학생/교수/관리자 - 역할별 필터)
    @GetMapping
    public ResponseEntity<ResultResponse<Page<AnnoListRes>>> getList(
            @ModelAttribute AnnoListReq req,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(new ResultResponse<>("공지사항 목록 조회 성공",
                announcementService.getList(req, pageable)));
    }

    // ANNO-04 공지사항 상세 조회
    @GetMapping("/{annoId}")
    public ResponseEntity<ResultResponse<AnnoDetailRes>> getDetail(@PathVariable Long annoId) {
        return ResponseEntity.ok(new ResultResponse<>("공지사항 상세 조회 성공",
                announcementService.getDetail(annoId)));
    }
}