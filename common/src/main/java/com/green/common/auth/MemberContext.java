package com.green.common.auth;

import com.green.common.model.MemberDto;

public class MemberContext {
    private static final ThreadLocal<MemberDto> USER_HOLDER = new ThreadLocal<>();

    public static void set(MemberDto user) { USER_HOLDER.set(user); }
    public static MemberDto get() { return USER_HOLDER.get(); }
    public static void clear() { USER_HOLDER.remove(); }
}