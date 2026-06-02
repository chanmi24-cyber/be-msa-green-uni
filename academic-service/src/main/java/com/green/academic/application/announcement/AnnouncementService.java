package com.green.academic.application.announcement;

import com.green.academic.application.announcement.model.*;
import com.green.academic.entity.Announcement;
import com.green.academic.enumcode.EnumTargetRole;
import com.green.academic.exception.AnnouncementErrorCode;
import com.green.common.auth.MemberContext;
import com.green.common.exception.BusinessException;
import com.green.common.model.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public List<Integer> getAnnouncementYears() {
        MemberDto member = MemberContext.get();
        return switch (member.role()) {
            case ADMIN     -> announcementRepository.findAllDistinctYears();
            case PROFESSOR -> announcementRepository.findDistinctYearsByRoles(List.of("PROFESSOR", "MEMBER", "ALL"));
            case STUDENT   -> announcementRepository.findDistinctYearsByRoles(List.of("STUDENT", "MEMBER", "ALL"));
        };
    }

    public List<Integer> getPublicAnnouncementYears() {
        return announcementRepository.findDistinctYearsByRoles(List.of("ALL"));
    }

    @Transactional
    public AnnoCreateRes create(AnnoCreateReq req) {
        Announcement anno = Announcement.builder()
                .memberCode(MemberContext.get().memberCode())
                .writerName(req.getWriterName())
                .targetRole(req.getTargetRole())
                .title(req.getTitle())
                .content(req.getContent())
                .build();
        announcementRepository.save(anno);
        return new AnnoCreateRes(anno.getAnnoId(), anno.getTargetRole().getCode(),
                anno.getTitle(), anno.getCreatedAt());
    }

    @Transactional
    public void update(Long annoId, AnnoUpdateReq req) {
        Announcement anno = findOwnAnnouncement(annoId);
        anno.update(req.getTitle(), req.getContent());
    }

    @Transactional
    public void delete(Long annoId) {
        Announcement anno = findOwnAnnouncement(annoId);
        anno.softDelete();
    }

    public Page<AnnoListRes> getList(AnnoListReq req, Pageable pageable) {
        MemberDto member = MemberContext.get();
        String search       = req.getSearch() != null && !req.getSearch().isBlank() ? req.getSearch() : null;
        LocalDateTime startDate = req.getYear() != null ? LocalDateTime.of(req.getYear(), 1, 1, 0, 0) : null;
        LocalDateTime endDate   = req.getYear() != null ? LocalDateTime.of(req.getYear() + 1, 1, 1, 0, 0) : null;

        Page<Announcement> page = switch (member.role()) {
            case ADMIN -> {
                EnumTargetRole roleFilter = req.getTargetRole() != null && !req.getTargetRole().isBlank()
                        ? EnumTargetRole.valueOf(req.getTargetRole()) : null;
                yield (roleFilter != null)
                        ? announcementRepository.findAdminByRoleWithFilters(roleFilter, search, startDate, endDate, pageable)
                        : announcementRepository.findAdminAllWithFilters(search, startDate, endDate, pageable);
            }
            case PROFESSOR -> announcementRepository.findByRolesWithFilters(
                        List.of(EnumTargetRole.PROFESSOR, EnumTargetRole.MEMBER, EnumTargetRole.ALL),
                        search, startDate, endDate, pageable);
            case STUDENT   -> announcementRepository.findByRolesWithFilters(
                        List.of(EnumTargetRole.STUDENT, EnumTargetRole.MEMBER, EnumTargetRole.ALL),
                        search, startDate, endDate, pageable);
        };
        return page.map(a -> new AnnoListRes(a.getAnnoId(), a.getTargetRole().getCode(),
                a.getTitle(), a.getWriterName(), a.getMemberCode(), a.getViewCount(), a.getCreatedAt()));
    }

    @Transactional
    public AnnoDetailRes getDetail(Long annoId) {
        MemberDto member = MemberContext.get();
        Announcement anno = announcementRepository.findByAnnoIdAndIsDelFalse(annoId)
                .orElseThrow(() -> new BusinessException(AnnouncementErrorCode.ANNOUNCEMENT_NOT_FOUND));
        checkAccess(member, anno);
        announcementRepository.incrementViewCount(annoId);
        return toDetailRes(announcementRepository.findByAnnoIdAndIsDelFalse(annoId)
                .orElseThrow(() -> new BusinessException(AnnouncementErrorCode.ANNOUNCEMENT_NOT_FOUND)));
    }

    public Page<AnnoListRes> getPublicList(String search, Integer year, Pageable pageable) {
        String searchParam  = search != null && !search.isBlank() ? search : null;
        LocalDateTime startDate = year != null ? LocalDateTime.of(year, 1, 1, 0, 0) : null;
        LocalDateTime endDate   = year != null ? LocalDateTime.of(year + 1, 1, 1, 0, 0) : null;
        return announcementRepository.findByRolesWithFilters(
                        List.of(EnumTargetRole.ALL), searchParam, startDate, endDate, pageable)
                .map(a -> new AnnoListRes(a.getAnnoId(), a.getTargetRole().getCode(),
                        a.getTitle(), a.getWriterName(), a.getMemberCode(), a.getViewCount(), a.getCreatedAt()));
    }

    @Transactional
    public AnnoDetailRes getPublicDetail(Long annoId) {
        Announcement anno = announcementRepository.findByAnnoIdAndIsDelFalse(annoId)
                .orElseThrow(() -> new BusinessException(AnnouncementErrorCode.ANNOUNCEMENT_NOT_FOUND));
        if (anno.getTargetRole() != EnumTargetRole.ALL) {
            throw new BusinessException(AnnouncementErrorCode.ANNOUNCEMENT_ACCESS_DENIED);
        }
        announcementRepository.incrementViewCount(annoId);
        return toDetailRes(announcementRepository.findByAnnoIdAndIsDelFalse(annoId)
                .orElseThrow(() -> new BusinessException(AnnouncementErrorCode.ANNOUNCEMENT_NOT_FOUND)));
    }

    private Announcement findOwnAnnouncement(Long annoId) {
        Announcement anno = announcementRepository.findByAnnoIdAndIsDelFalse(annoId)
                .orElseThrow(() -> new BusinessException(AnnouncementErrorCode.ANNOUNCEMENT_NOT_FOUND));
        if (!anno.getMemberCode().equals(MemberContext.get().memberCode())) {
            throw new BusinessException(AnnouncementErrorCode.ANNOUNCEMENT_ACCESS_DENIED);
        }
        return anno;
    }

    private void checkAccess(MemberDto member, Announcement anno) {
        boolean allowed = switch (member.role()) {
            case ADMIN     -> true;
            case PROFESSOR -> anno.getTargetRole() == EnumTargetRole.PROFESSOR
                             || anno.getTargetRole() == EnumTargetRole.ALL;
            case STUDENT   -> anno.getTargetRole() == EnumTargetRole.STUDENT
                             || anno.getTargetRole() == EnumTargetRole.ALL;
        };
        if (!allowed) throw new BusinessException(AnnouncementErrorCode.ANNOUNCEMENT_ACCESS_DENIED);
    }

    private AnnoDetailRes toDetailRes(Announcement anno) {
        return new AnnoDetailRes(
                anno.getAnnoId(), anno.getTargetRole().getCode(),
                anno.getTitle(), anno.getContent(), anno.getWriterName(), anno.getMemberCode(),
                anno.getViewCount(), anno.getCreatedAt(), anno.getUpdatedAt(),
                List.of()
        );
    }
}