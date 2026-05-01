package com.green.common.auth;

import com.green.common.model.MemberDto;
// 현재 요청을 처리하는 동안 로그인 유저 정보를 보관하는 저장소
// ThreadLocal을 사용해 요청마다 독립된 공간에 저장 (다른 요청과 섞이지 않음)
public class MemberContext {
    private static final ThreadLocal<MemberDto> USER_HOLDER = new ThreadLocal<>();

    public static void set(MemberDto member) { USER_HOLDER.set(member); } // 저장
    public static MemberDto get() { return USER_HOLDER.get(); } // 조회
    public static void clear() { USER_HOLDER.remove(); } //삭제 (요청 완료 후 반드시 호출)
}