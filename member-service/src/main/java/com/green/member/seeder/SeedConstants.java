package com.green.member.seeder;

import java.util.List;

public final class SeedConstants {
    private SeedConstants() {}

    // ⚠️ 반드시 List 선언 — Set/Map 금지 (순서 고정 필수)
    public static final List<Long> MAJOR_IDS = List.of(
            1001L, 1002L, 1003L,                    // 인문대학
            1004L, 1005L, 1006L,                    // 자연과학대학
            1007L, 1008L, 1009L,                    // 사회과학대학
            1010L, 1011L, 1012L, 1013L, 1014L,     // 공과대학
            1015L, 1016L, 1017L,                    // 예술대학
            1018L, 1019L, 1020L,                    // 경영대학
            1021L, 1022L, 1023L,                    // 사범대학
            1024L, 1025L                             // 체육대학
    );

    public static final List<String> MAJOR_NAMES = List.of(
            "국어국문학과", "영어영문학과", "철학과",
            "수학과", "물리학과", "화학과",
            "사회학과", "심리학과", "행정학과",
            "컴퓨터공학과", "소프트웨어공학과", "기계공학과", "전자공학과", "건축공학과",
            "시각디자인학과", "음악학과", "미술학과",
            "경영학과", "회계학과", "무역학과",
            "교육학과", "수학교육학과", "영어교육학과",
            "체육학과", "스포츠과학과"
    );

    // MAJOR_IDS 순서와 1:1 대응
    public static final List<Long> COLLEGE_IDS = List.of(
            1L, 1L, 1L,
            2L, 2L, 2L,
            3L, 3L, 3L,
            4L, 4L, 4L, 4L, 4L,
            5L, 5L, 5L,
            6L, 6L, 6L,
            7L, 7L, 7L,
            8L, 8L
    );

    public static final List<String> COLLEGE_NAMES = List.of(
            "인문대학", "인문대학", "인문대학",
            "자연과학대학", "자연과학대학", "자연과학대학",
            "사회과학대학", "사회과학대학", "사회과학대학",
            "공과대학", "공과대학", "공과대학", "공과대학", "공과대학",
            "예술대학", "예술대학", "예술대학",
            "경영대학", "경영대학", "경영대학",
            "사범대학", "사범대학", "사범대학",
            "체육대학", "체육대학"
    );

    // 결정론적 시스템 관리자 코드 (year=2020, role=3, seq=001)
    // AuthDataSeeder / CoreDataSeeder에서도 동일 상수 사용
    public static final Long SYSTEM_ADMIN_CODE = 20203001L;
}
