package com.green.common.security;

import com.green.common.auth.AuthErrorCode;
import com.green.common.constants.ConstJwt;
import com.green.common.exception.BusinessException;
import com.green.common.model.JwtMember;
import com.green.common.model.UserPrincipal;
import com.green.common.redis.RedisService;
import com.green.common.utils.MyCookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(Authentication.class) //클래스패스에 특정 클래스(예: Authentication)가 존재할 때만 빈으로 등록
public class JwtTokenManager { //인증처리 총괄
    private final ConstJwt constJwt;
    private final MyCookieUtil myCookieUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    // 로그인 성공 시 AT/RT 모두 발급
    public void issue(HttpServletResponse res, JwtMember jwtMember){
        setAccessTokenInCookie(res, jwtMember); // AT -> 쿠키에 저장
        setRefreshTokenInCookie(res, jwtMember); //RT -> 쿠키&Redis에 저장
    }

    // AT를 새롭게 생성후 쿠키에 저장
    public void setAccessTokenInCookie(HttpServletResponse res, JwtMember jwtMember){
        String accessToken = jwtTokenProvider.generateAccessToken(jwtMember); // 토큰 생성
        setAccessTokenInCookie(res, accessToken); // 만들어진 AT 문자열을 쿠키에 담는 메소드 호출
    }

    // RT를 새롭게 생성 후 쿠키 + Redis에 저장
    public void setRefreshTokenInCookie(HttpServletResponse res, JwtMember jwtMember){
        String refreshToken = jwtTokenProvider.generateRefreshToken(jwtMember);
        String redisKey = String.format("RT-%d:%s", jwtMember.getLoginMemberCode(), jwtMember.getDeviceId());  // Redis 키 형식: "RT-{memberCode}
        redisService.save(redisKey, refreshToken, constJwt.refreshTokenCookieValiditySeconds()); // Redis에 RT 문자열 저장. 재발급 시 검증을 위해
        setRefreshTokenInCookie(res, refreshToken); // 만들어진 RT 문자열을 쿠키에 담는 메소드 호출
    }

//    public String generateRefreshToken(JwtMember jwtMember) {
//        return jwtTokenProvider.generateRefreshToken(jwtMember);
//    }

    // 만들어진 AT 문자열을 쿠키에 담기
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

    // 요청 쿠키에서 AT꺼내기. 이후 유저 정보 확인할 때 사용
    public String getAccessTokenFromCookie(HttpServletRequest req){
        return myCookieUtil.getValue(req, constJwt.accessTokenCookieName());
    }

    // 요청 쿠키에서 RT꺼내기. 재발급과 로그아웃에서 사용
    public String getRefreshTokenFromCookie(HttpServletRequest req) {
        return myCookieUtil.getValue(req, constJwt.refreshTokenCookieName());
    }

    // 쿠키에 든 AT를 꺼내 인증 객체로 변환. 모든 요청마다 이 메서드로 로그인상태 확인
    public Authentication getAuthentication(HttpServletRequest req){
        String accessToken = getAccessTokenFromCookie(req); // req의 쿠키 속 AT 꺼내기

        // 쿠키에 AT 없다면(= 로그인 안된 상태), null 반환
        if(accessToken == null){ return null; }

        //쿠키에 AT가 있다면, JWT에 담았던 JwtMember객체(로그인 유저 정보)를 빼내어, 시큐리티를 위한 UserPrincipal로 변환
        JwtMember jwtMember = jwtTokenProvider.getJwtMemberFromToken(accessToken);
        UserPrincipal userPrincipal = new UserPrincipal(jwtMember);
        log.info("userPrincipal: {}", userPrincipal);
        // 시큐리티 인증된 로그인 유저 정보(로그인ID, role 담아 반환. @AuthenticationPrincipal로 활용)
        return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    // AT 삭제
    public void deleteAccessTokenInCookie(HttpServletResponse res){
        myCookieUtil.deleteCookie(res, constJwt.accessTokenCookieName(), constJwt.accessTokenCookiePath());
    }

    // RT 삭제
    public void deleteRefreshTokenInCookie(HttpServletResponse res){
        myCookieUtil.deleteCookie(res, constJwt.refreshTokenCookieName(), constJwt.refreshTokenCookiePath());
    }

    // 로그아웃할 때 쿠키과 redis에 저장한 토큰 삭제
    public void logOut(HttpServletRequest req, HttpServletResponse res){
        String refreshToken = getRefreshTokenFromCookie(req);
        if (refreshToken != null) {
            // RT에서 redis 키 조합
            JwtMember jwtMember = jwtTokenProvider.getJwtMemberFromToken(refreshToken);
            String redisKey = String.format("RT-%d:%s", jwtMember.getLoginMemberCode(), jwtMember.getDeviceId());
            // redis에서 해당 기기 RT 삭제
            redisService.delete(redisKey);
        }
        // Redis 삭제 성공 여부와 관계없이 쿠키는 항상 삭제
        deleteAccessTokenInCookie(res);
        deleteRefreshTokenInCookie(res);
    }

    // 쿠키의 RT로 새 AT 재발급
    public void reissue(HttpServletRequest req, HttpServletResponse res) {
        // 쿠키에서 RT을 얻기
        String refreshToken = getRefreshTokenFromCookie(req);

        // RT를 이용하여 JwtMember 객체 꺼내기
        JwtMember jwtMember = jwtTokenProvider.getJwtMemberFromToken(refreshToken);

        // Redis 키 조합 후 저장된 RT 조회
        String redisKey = String.format("RT-%d:%s", jwtMember.getLoginMemberCode(), jwtMember.getDeviceId());
        String savedToken = redisService.get(redisKey, String.class);

        // Redis에 저장된 RT와 쿠키의 RT가 다르면 위변조된 토큰으로 판단해 예외 발생
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 검증 통과 시 새 AT 생성 후 쿠키에 저장
        setAccessTokenInCookie(res, jwtMember);
    }
}