package com.green.academic.application.announcement;

import com.green.academic.application.announcement.model.AnnoDetailRes;
import com.green.academic.application.announcement.model.AnnoListRes;
import com.green.common.model.ResultResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/announcements")
public class PublicAnnouncementController {

    private final AnnouncementService announcementService;

    // 공개 공지사항이 존재하는 연도 목록
    @GetMapping("/years")
    public ResponseEntity<ResultResponse<List<Integer>>> getPublicYears() {
        return ResponseEntity.ok(new ResultResponse<>("연도 조회 성공",
                announcementService.getPublicAnnouncementYears()));
    }

    // ANNO-06 공개 공지사항 목록 조회 (비로그인, targetRole=ALL만)
    @GetMapping
    public ResponseEntity<ResultResponse<Page<AnnoListRes>>> getPublicList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer year,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(new ResultResponse<>("공개 공지사항 목록 조회 성공",
                announcementService.getPublicList(search, year, pageable)));
    }

    // ANNO-07 공개 공지사항 상세 조회
    @GetMapping("/{annoId}")
    public ResponseEntity<ResultResponse<AnnoDetailRes>> getPublicDetail(@PathVariable Long annoId) {
        return ResponseEntity.ok(new ResultResponse<>("공개 공지사항 상세 조회 성공",
                announcementService.getPublicDetail(annoId)));
    }
}