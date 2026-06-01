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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

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
        Page<Announcement> page = switch (member.role()) {
            case ADMIN -> {
                EnumTargetRole roleFilter = req.getTargetRole() != null
                        ? EnumTargetRole.valueOf(req.getTargetRole()) : null;
                String search = req.getSearch() != null && !req.getSearch().isBlank()
                        ? req.getSearch() : null;
                yield (roleFilter != null && search != null)
                        ? announcementRepository.findByTargetRoleAndSearchAndIsDelFalse(roleFilter, search, pageable)
                        : (roleFilter != null)
                        ? announcementRepository.findByTargetRoleAndIsDelFalseOrderByCreatedAtDesc(roleFilter, pageable)
                        : (search != null)
                        ? announcementRepository.findAllBySearchAndIsDelFalse(search, pageable)
                        : announcementRepository.findAllByIsDelFalseOrderByCreatedAtDesc(pageable);
            }
            case PROFESSOR -> announcementRepository.findByTargetRoleInAndIsDelFalseOrderByCreatedAtDesc(
                        List.of(EnumTargetRole.PROFESSOR, EnumTargetRole.ALL), pageable);
            case STUDENT   -> announcementRepository.findByTargetRoleInAndIsDelFalseOrderByCreatedAtDesc(
                        List.of(EnumTargetRole.STUDENT, EnumTargetRole.ALL), pageable);
        };
        return page.map(a -> new AnnoListRes(a.getAnnoId(), a.getTargetRole().getCode(),
                a.getTitle(), a.getWriterName(), a.getViewCount(), a.getCreatedAt()));
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

    public Page<AnnoListRes> getPublicList(Pageable pageable) {
        return announcementRepository.findByTargetRoleInAndIsDelFalseOrderByCreatedAtDesc(
                        List.of(EnumTargetRole.ALL), pageable)
                .map(a -> new AnnoListRes(a.getAnnoId(), a.getTargetRole().getCode(),
                        a.getTitle(), a.getWriterName(), a.getViewCount(), a.getCreatedAt()));
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
                anno.getTitle(), anno.getContent(), anno.getWriterName(),
                anno.getViewCount(), anno.getCreatedAt(), anno.getUpdatedAt(),
                List.of()
        );
    }
}