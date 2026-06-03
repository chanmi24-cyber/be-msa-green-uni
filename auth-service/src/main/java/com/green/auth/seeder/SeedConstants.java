package com.green.auth.seeder;

import java.util.List;

public final class SeedConstants {
    private SeedConstants() {}

    // ⚠️ 반드시 List 선언 — Set/Map 금지 (순서 고정 필수)
    // member-service / core-service와 완전히 동일한 상수 — 서비스 간 JVM 분리로 공유 불가
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
}
