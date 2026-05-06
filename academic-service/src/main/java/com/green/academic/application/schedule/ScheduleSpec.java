package com.green.academic.application.schedule;

import com.green.academic.entity.Schedule;
import com.green.academic.application.schedule.model.ScheduleListReq;
import org.springframework.data.jpa.domain.Specification;

public class ScheduleSpec {

    public static Specification<Schedule> filter(ScheduleListReq req) {
        return Specification
                .where(eqYear(req.getYear()))
                .and(eqSemester(req.getSemester()))
                .and(eqMonth(req.getTargetMonth()));
    }

    private static Specification<Schedule> eqYear(Integer year) {
        if (year == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("year"), year);
    }

    private static Specification<Schedule> eqSemester(Integer semester) {
        if (semester == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("semester"), semester);
    }

    private static Specification<Schedule> eqMonth(Integer targetMonth) {
        if (targetMonth == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(
                cb.function("MONTH", Integer.class, root.get("startDate")), targetMonth);
    }
}