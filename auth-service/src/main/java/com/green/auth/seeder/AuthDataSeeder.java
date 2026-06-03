package com.green.auth.seeder;

import com.green.auth.application.auth.AuthMemberRepository;
import com.green.auth.entity.AuthMember;
import com.green.common.enumcode.EnumMemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthDataSeeder implements CommandLineRunner {

    private final AuthMemberRepository authMemberRepository;
    private final SeederVersionRepository seederVersionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;

    private int flushCounter = 0;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (seederVersionRepository.existsById("AUTH_V1")) {
            log.info("[AuthDataSeeder] AUTH_V1 이미 실행됨, 건너뜀");
            return;
        }
        flushCounter = 0;
        log.info("[AuthDataSeeder] AUTH_V1 시작...");

        // BCrypt는 매번 다른 salt를 사용하므로 한 번만 인코딩하여 재사용
        String encodedPw = passwordEncoder.encode("1234");

        generateAdminAuth(encodedPw);      // ① 관리자 5명
        generateProfessorAuth(encodedPw);  // ② 교수 120명
        generateStudentAuth(encodedPw);    // ③ 학생 2850명

        seederVersionRepository.save(new SeederVersion("AUTH_V1", LocalDateTime.now()));
        log.info("[AuthDataSeeder] AUTH_V1 완료");
    }

    // ────────────────────────────────────────────────────────
    // ① 관리자 5명
    //    memberCode: year=2020, role=3, seq=001~005
    //    MemberDataSeeder와 동일한 결정론적 규칙
    // ────────────────────────────────────────────────────────
    private void generateAdminAuth(String encodedPw) throws InterruptedException {
        for (int seq = 1; seq <= 5; seq++) {
            long memberCode = Long.parseLong("2020" + "3" + String.format("%03d", seq));
            String email = "admin" + seq + "@green-uni.ac.kr";

            authMemberRepository.save(AuthMember.builder()
                    .memberCode(memberCode)
                    .password(encodedPw)
                    .role(EnumMemberRole.ADMIN)
                    .email(email)
                    .build());

            batchFlush();
        }
        log.info("[AuthDataSeeder] Admin 5명 생성");
    }

    // ────────────────────────────────────────────────────────
    // ② 교수 120명
    //    entryYear = 2020 + (i % 5), per-year seq 카운터
    //    MemberDataSeeder와 동일한 결정론적 규칙
    // ────────────────────────────────────────────────────────
    private void generateProfessorAuth(String encodedPw) throws InterruptedException {
        // ⚠️ DB count() 조회 금지 — per-year in-memory 카운터만 사용
        Map<Integer, Integer> seqByYear = new HashMap<>();

        for (int i = 0; i < 120; i++) {
            int entryYear = 2020 + (i % 5);
            int seq = seqByYear.getOrDefault(entryYear, 0) + 1;
            if (seq > 999) throw new IllegalStateException(
                    "Professor memberCode seq overflow: year=" + entryYear + ", seq=" + seq);
            seqByYear.put(entryYear, seq);

            long memberCode = Long.parseLong(entryYear + "2" + String.format("%03d", seq));
            String email = "p" + memberCode + "@green-uni.ac.kr";

            authMemberRepository.save(AuthMember.builder()
                    .memberCode(memberCode)
                    .password(encodedPw)
                    .role(EnumMemberRole.PROFESSOR)
                    .email(email)
                    .build());

            batchFlush();
        }
        log.info("[AuthDataSeeder] Professor 120명 생성");
    }

    // ────────────────────────────────────────────────────────
    // ③ 학생 2850명
    //    2024/2025/2026학번 각 875명 일반 + 75명 편입생
    //    seq: 전역 카운터 (학과 무관), MemberDataSeeder와 동일한 결정론적 규칙
    //    email prefix: 일반=s, 편입=t (MemberDataSeeder와 동일)
    // ────────────────────────────────────────────────────────
    private void generateStudentAuth(String encodedPw) throws InterruptedException {
        int[] entryYears = {2024, 2025, 2026};

        for (int year : entryYears) {

            // ── 일반 학생 (seq 1 ~ 875) ──────────────────────────
            int seq = 1;
            for (int majorIdx = 0; majorIdx < SeedConstants.MAJOR_IDS.size(); majorIdx++) {
                for (int i = 0; i < 35; i++) {
                    if (seq > 999) throw new IllegalStateException(
                            "Student seq overflow: year=" + year + ", seq=" + seq);

                    long memberCode = Long.parseLong(year + "1" + String.format("%03d", seq));
                    String email = "s" + memberCode + "@green-uni.ac.kr";

                    authMemberRepository.save(AuthMember.builder()
                            .memberCode(memberCode)
                            .password(encodedPw)
                            .role(EnumMemberRole.STUDENT)
                            .email(email)
                            .build());

                    seq++;
                    batchFlush();
                }
            }

            // ── 편입생 (seq 901 ~ 975) ───────────────────────────
            int transferSeq = 901;
            for (int majorIdx = 0; majorIdx < SeedConstants.MAJOR_IDS.size(); majorIdx++) {
                for (int t = 0; t < 3; t++) {
                    if (transferSeq > 975) throw new IllegalStateException(
                            "Transfer seq overflow: year=" + year + ", seq=" + transferSeq);

                    long memberCode = Long.parseLong(year + "1" + String.format("%03d", transferSeq));
                    String email = "t" + memberCode + "@green-uni.ac.kr";

                    authMemberRepository.save(AuthMember.builder()
                            .memberCode(memberCode)
                            .password(encodedPw)
                            .role(EnumMemberRole.STUDENT)
                            .email(email)
                            .build());

                    transferSeq++;
                    batchFlush();
                }
            }

            log.info("[AuthDataSeeder] {}학번 일반 875명 + 편입생 75명 생성", year);
        }
    }

    // ────────────────────────────────────────────────────────
    // 500건 단위 flush/clear (OOM 방지)
    // Thread.sleep(1): TSID 초당 16,384개 한계 대응
    // ────────────────────────────────────────────────────────
    private void batchFlush() throws InterruptedException {
        flushCounter++;
        if (flushCounter % 500 == 0) {
            em.flush();
            em.clear();
            Thread.sleep(1);
        }
    }
}
