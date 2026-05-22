package com.green.academic.application.schedule;

import com.green.academic.entity.Schedule;
import com.green.academic.kafka.AcademicNotificationProducer;
import com.green.common.enumcode.EnumMemberRole;
import com.green.common.enumcode.EnumScheduleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleNotificationScheduler {

    private final ScheduleRepository scheduleRepository;
    private final AcademicNotificationProducer notificationProducer;

    @Scheduled(cron = "0 * * * * *")
    public void sendScheduleNotifications() {
        LocalDate today = LocalDate.now();
        List<Schedule> schedules = scheduleRepository.findAll();

        for (Schedule schedule : schedules) {
            LocalDate startDate = schedule.getStartDate().toLocalDate();
            LocalDate endDate = schedule.getEndDate().toLocalDate();

            // 이미 종료된 스케줄은 skip
            if (today.isAfter(endDate)) continue;

            processStartNotification(schedule, today, startDate);
            processDeadlineNotifications(schedule, today, startDate, endDate);
        }

        log.info("학사일정 알림 처리 완료: {}건", schedules.size());
    }

    private void processStartNotification(Schedule schedule, LocalDate today, LocalDate startDate) {
        if (today.isBefore(startDate)) return;

        int updated = scheduleRepository.markNotifiedStartIfFalse(schedule.getScheduleId());
        if (updated == 0) return;

        EnumScheduleType type = schedule.getType();
        String message = buildStartMessage(type, schedule.getSemester());
        if (message == null) return;

        if (type == EnumScheduleType.SEMESTER_START) {
            String url = buildUrl(type, schedule);
            notificationProducer.sendToRole(type.getCode() + "_START", message, EnumMemberRole.STUDENT, schedule.getScheduleId(), url);
            notificationProducer.sendToRole(type.getCode() + "_START", message, EnumMemberRole.PROFESSOR, schedule.getScheduleId(), url);
        } else {
            EnumMemberRole targetRole = resolveTargetRole(type);
            if (targetRole != null) {
                notificationProducer.sendToRole(type.getCode() + "_START", message, targetRole, schedule.getScheduleId(), buildUrl(type, schedule));
            }
        }
        // 관리자 알림
        notificationProducer.sendToRole(
                type.getCode() + "_START",
                "[관리자] " + message,
                EnumMemberRole.ADMIN,
                schedule.getScheduleId(),
                buildAdminUrl(type, schedule));
    }

    private void processDeadlineNotifications(Schedule schedule, LocalDate today, LocalDate startDate, LocalDate endDate) {
        // 마감 리마인더 없는 타입
        if (schedule.getType() == EnumScheduleType.GRADE_VIEW
                || schedule.getType() == EnumScheduleType.SEMESTER_START
                || schedule.getType() == EnumScheduleType.ETC) return;

        EnumMemberRole targetRole = resolveTargetRole(schedule.getType());
        if (targetRole == null) return;

        // 3일 전 알림 (기간이 3일 이상인 경우만)
        LocalDate threeDaysBefore = endDate.minusDays(3);
        if (today.equals(threeDaysBefore) && !today.isBefore(startDate)) {
            int updated = scheduleRepository.markNotifiedThreeDaysBeforeIfFalse(schedule.getScheduleId());
            if (updated > 0) {
                String message = buildThreeDaysMessage(schedule.getType());
                if (message != null) {
                    notificationProducer.sendToRole(
                            schedule.getType().getCode() + "_3DAYS",
                            message, targetRole, schedule.getScheduleId(), buildUrl(schedule.getType(), schedule));
                    String adminThreeDaysMessage = buildAdminThreeDaysMessage(schedule.getType());
                    if (adminThreeDaysMessage != null) {
                        notificationProducer.sendToRole(
                                schedule.getType().getCode() + "_3DAYS",
                                adminThreeDaysMessage,
                                EnumMemberRole.ADMIN,
                                schedule.getScheduleId(),
                                buildAdminUrl(schedule.getType(), schedule));
                    }
                }
            }
        }

        // 마지막 날 알림
        if (today.equals(endDate)) {
            int updated = scheduleRepository.markNotifiedEndIfFalse(schedule.getScheduleId());
            if (updated > 0) {
                String message = buildLastDayMessage(schedule.getType());
                if (message != null) {
                    notificationProducer.sendToRole(
                            schedule.getType().getCode() + "_LAST_DAY",
                            message, targetRole, schedule.getScheduleId(), buildUrl(schedule.getType(), schedule));
                    String adminLastDayMessage = buildAdminLastDayMessage(schedule.getType());
                    if (adminLastDayMessage != null) {
                        notificationProducer.sendToRole(
                                schedule.getType().getCode() + "_LAST_DAY",
                                adminLastDayMessage,
                                EnumMemberRole.ADMIN,
                                schedule.getScheduleId(),
                                buildAdminUrl(schedule.getType(), schedule));
                    }
                }
            }
        }
    }

    private EnumMemberRole resolveTargetRole(EnumScheduleType type) {
        return switch (type) {
            case COURSE_REGISTRATION, COURSE_MODIFICATION, GRADE_VIEW,
                 GRADE_APPEAL, LECTURE_EVALUATION, TUITION_PAYMENT, MAJOR_CHANGE -> EnumMemberRole.STUDENT;
            case GRADE_INPUT, LECTURE_REGISTRATION -> EnumMemberRole.PROFESSOR;
            default -> null;
        };
    }

    private String buildStartMessage(EnumScheduleType type, int semester) {
        return switch (type) {
            case COURSE_REGISTRATION -> "수강신청이 시작되었습니다.";
            case COURSE_MODIFICATION -> "수강정정이 시작되었습니다.";
            case GRADE_INPUT -> "성적 입력 기간이 시작되었습니다. 성적을 입력해 주세요.";
            case GRADE_VIEW -> "성적이 공개되었습니다. 성적을 확인하세요!";
            case GRADE_APPEAL -> "성적 이의신청 기간이 시작되었습니다.";
            case LECTURE_EVALUATION -> "강의평가 기간이 시작되었습니다.";
            case TUITION_PAYMENT -> "등록금 납부 기간이 시작되었습니다.";
            case LECTURE_REGISTRATION -> "강의 개설 신청 기간이 시작되었습니다.";
            case MAJOR_CHANGE -> "전과 신청 기간이 시작되었습니다.";
            case SEMESTER_START -> semester + "학기가 시작되었습니다. 즐거운 대학생활 되세요!";
            default -> null;
        };
    }

    private String buildThreeDaysMessage(EnumScheduleType type) {
        return switch (type) {
            case COURSE_REGISTRATION -> "수강신청 마감 3일 전입니다. 서둘러 신청하세요!";
            case COURSE_MODIFICATION -> "수강정정 마감 3일 전입니다. 정정 사항을 확인하세요!";
            case GRADE_INPUT -> "성적 입력 마감 3일 전입니다. 미입력 강의 성적을 입력해 주세요.";
            case GRADE_APPEAL -> "성적 이의신청 마감 3일 전입니다.";
            case LECTURE_EVALUATION -> "강의평가 마감 3일 전입니다. 아직 평가하지 않은 강의를 평가해 주세요!";
            case TUITION_PAYMENT -> "등록금 납부 마감 3일 전입니다. 기한 내 납부해 주세요.";
            case LECTURE_REGISTRATION -> "강의 개설 신청 마감 3일 전입니다.";
            case MAJOR_CHANGE -> "전과 신청 마감 3일 전입니다.";
            default -> null;
        };
    }

    private String buildLastDayMessage(EnumScheduleType type) {
        return switch (type) {
            case COURSE_REGISTRATION -> "수강신청 마지막 날입니다. 지금 바로 신청하세요!";
            case COURSE_MODIFICATION -> "수강정정 마지막 날입니다.";
            case GRADE_INPUT -> "성적 입력 마지막 날입니다. 오늘까지 입력해 주세요.";
            case GRADE_APPEAL -> "성적 이의신청 마지막 날입니다.";
            case LECTURE_EVALUATION -> "강의평가 마지막 날입니다. 지금 바로 평가해 주세요!";
            case TUITION_PAYMENT -> "등록금 납부 마지막 날입니다. 오늘까지 납부해 주세요.";
            case LECTURE_REGISTRATION -> "강의 개설 신청 마지막 날입니다.";
            case MAJOR_CHANGE -> "전과 신청 마지막 날입니다.";
            default -> null;
        };
    }

    private String buildAdminThreeDaysMessage(EnumScheduleType type) {
        return switch (type) {
            case GRADE_INPUT         -> "[관리자] 성적 입력 마감 3일 전입니다. 미입력 교수님을 확인해주세요.";
            case TUITION_PAYMENT     -> "[관리자] 등록금 납부 마감 3일 전입니다. 미납 학생을 확인해주세요.";
            case COURSE_REGISTRATION -> "[관리자] 수강신청 마감 3일 전입니다. 미신청 학생을 확인해주세요.";
            case LECTURE_EVALUATION  -> "[관리자] 강의평가 마감 3일 전입니다. 미평가 학생을 확인해주세요.";
            case LECTURE_REGISTRATION -> "[관리자] 강의 개설 신청 마감 3일 전입니다. 개설이 안된 강의가 있는지 확인해주세요.";
            case COURSE_MODIFICATION -> "[관리자] 수강정정 마감 3일 전입니다.";
            case GRADE_APPEAL        -> "[관리자] 성적 이의신청 마감 3일 전입니다.";
            case MAJOR_CHANGE        -> "[관리자] 전과 신청 마감 3일 전입니다.";
            default -> null;
        };
    }

    private String buildAdminLastDayMessage(EnumScheduleType type) {
        return switch (type) {
            case GRADE_INPUT         -> "[관리자] 성적 입력 마지막 날입니다. 미입력 교수님을 오늘까지 확인해주세요.";
            case TUITION_PAYMENT     -> "[관리자] 등록금 납부 마지막 날입니다. 미납 학생을 오늘까지 확인해주세요.";
            case COURSE_REGISTRATION -> "[관리자] 수강신청 마지막 날입니다. 미신청 학생을 오늘까지 확인해주세요.";
            case LECTURE_EVALUATION  -> "[관리자] 강의평가 마지막 날입니다. 미평가 학생을 오늘까지 확인해주세요.";
            case LECTURE_REGISTRATION -> "[관리자] 강의 개설 신청 마지막 날입니다. 개설이 안된 강의가 있는지 오늘까지 확인해주세요.";
            case COURSE_MODIFICATION -> "[관리자] 수강정정 마지막 날입니다.";
            case GRADE_APPEAL        -> "[관리자] 성적 이의신청 마지막 날입니다.";
            case MAJOR_CHANGE        -> "[관리자] 전과 신청 마지막 날입니다.";
            default -> null;
        };
    }

    private String buildUrl(EnumScheduleType type, Schedule schedule) {
        String qs = "?year=" + schedule.getYear() + "&semester=" + schedule.getSemester() + "&page=1";
        return switch (type) {
            case LECTURE_EVALUATION  -> "/evaluations" + qs;
            case LECTURE_REGISTRATION -> "/lectures/my" + qs;
            case COURSE_REGISTRATION -> "/courses";
            case COURSE_MODIFICATION -> "/courses";
            case GRADE_INPUT         -> "/grades";
            case GRADE_VIEW          -> "/grades/my";
            case GRADE_APPEAL        -> "/grades/appeal/my";
            case TUITION_PAYMENT     -> "/tuitions/my";
            case MAJOR_CHANGE        -> "/members/major-request";
            case SEMESTER_START      -> "/";
            default -> "/";
        };
    }

    private String buildAdminUrl(EnumScheduleType type, Schedule schedule) {
        String qs = "?year=" + schedule.getYear() + "&semester=" + schedule.getSemester() + "&page=1";
        return switch (type) {
            case MAJOR_CHANGE    -> "/members/major-request";
            case TUITION_PAYMENT -> "/tuitions" + qs;
            default -> "/";
        };
    }
}
