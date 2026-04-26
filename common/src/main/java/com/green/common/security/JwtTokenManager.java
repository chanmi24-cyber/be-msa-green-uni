package com.green.common.security;

import com.green.common.constants.ConstJwt;
import com.green.common.model.JwtMember;
import com.green.common.model.UserPrincipal;
import com.green.common.utils.MyCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenManager { //인증처리 총괄
    private final ConstJwt constJwt;
    private final MyCookieUtil myCookieUtil;
    private final JwtTokenProvider jwtTokenProvider;

    // 토큰을 쿠키에 담아 응답으로 내보내기
    public void issue(HttpServletResponse res, JwtMember jwtMember){
        setAccessTokenInCookie(res, jwtMember);
        setRefreshTokenInCookie(res, jwtMember);
    }

    // AT를 새롭게 생성후 쿠키에 담기
    public void setAccessTokenInCookie(HttpServletResponse res, JwtMember jwtMember){
        String accessToken = jwtTokenProvider.generateAccessToken(jwtMember); // 토큰 생성
        setAccessTokenInCookie(res, accessToken); // 만들어진 AT를 쿠키에 담는 메소드 호출
    }
    // RT를 새롭게 생성후 쿠키에 담기
    public void setRefreshTokenInCookie(HttpServletResponse res, JwtMember jwtMember){
        String refreshToken = jwtTokenProvider.generateRefreshToken(jwtMember);
        setRefreshTokenInCookie(res, refreshToken); // 만들어진 RT를 쿠키에 담는 메소드 호출
    }

    public String generateRefreshToken(JwtMember jwtMember) {
        return jwtTokenProvider.generateRefreshToken(jwtMember);
    }

    // 만들어진 AT를 쿠키에 담기
    public void setAccessTokenInCookie(HttpServletResponse res, String accessToken){
        myCookieUtil.setCookie(res,
                constJwt.accessTokenCookieName(),
                accessToken,
                constJwt.accessTokenCookieValiditySeconds(),
                constJwt.accessTokenCookiePath()
        );
    }

    // 만들어진 RT를 쿠키에 담기
    public void setRefreshTokenInCookie(HttpServletResponse res, String refreshToken){
        myCookieUtil.setCookie(res,
                constJwt.refreshTokenCookieName(),
                refreshToken,
                constJwt.refreshTokenCookieValiditySeconds(),
                constJwt.refreshTokenCookiePath()
        );
    }

    // 쿠키에서 AT꺼내기
    public String getAccessTokenFromCookie(HttpServletRequest req){
        return myCookieUtil.getValue(req, constJwt.accessTokenCookieName());
    }

    // 쿠키에서 RT꺼내기
    public String getRefreshTokenFromCookie(HttpServletRequest req) {
        return myCookieUtil.getValue(req, constJwt.refreshTokenCookieName());
    }

    // 쿠키에 든 AT를 확인
    public Authentication getAuthentication(HttpServletRequest req){
        String accessToken = getAccessTokenFromCookie(req); // req의 쿠키 속 AT 꺼내기

        // 쿠키에 AT 없다면(= 로그인 안된 상태), null 반환
        if(accessToken == null){ return null; }

        //쿠키에 AT가 있다면, JWT에 담았던 JwtMember객체(로그인 유저 정보)를 빼내어, 시큐리티를 위한 UserPrincipal로 변환
        JwtMember jwtMember = jwtTokenProvider.getJwtMemberFromToken(accessToken);
        UserPrincipal userPrincipal = new UserPrincipal(jwtMember);

        return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        // 시큐리티 인증된 로그인 유저 정보(로그인ID, role 담아 반환. @AuthenticationPrincipal로 활용)
    }

    // AT 삭제
    public void deleteAccessTokenInCookie(HttpServletResponse res){
        myCookieUtil.deleteCookie(res, constJwt.accessTokenCookieName(), constJwt.accessTokenCookiePath());
    }

    // RT 삭제
    public void deleteRefreshTokenInCookie(HttpServletResponse res){
        myCookieUtil.deleteCookie(res, constJwt.refreshTokenCookieName(), constJwt.refreshTokenCookiePath());
    }

    // 로그아웃할 때 AT, RT 삭제
    public void logOut(HttpServletResponse res){
        deleteAccessTokenInCookie(res);
        deleteRefreshTokenInCookie(res);
    }

    // AT 재발행
    public void reissue(HttpServletRequest req, HttpServletResponse res) {
        //req에서 RT을 얻기
        String refreshToken = getRefreshTokenFromCookie(req);

        //RT를 이용하여 JwtUser 객체 생성
        JwtMember jwtMember = jwtTokenProvider.getJwtMemberFromToken(refreshToken);

        //JwtMember를 이용해 AT를 만들어 cookie에 담기
        setAccessTokenInCookie(res, jwtMember);
    }
}
