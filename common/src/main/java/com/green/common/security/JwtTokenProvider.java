package com.green.common.security;

import com.green.common.constants.ConstJwt;
import com.green.common.model.JwtMember;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@ConditionalOnClass(Authentication.class) //클래스패스에 특정 클래스(예: Authentication)가 존재할 때만 빈으로 등록
public class JwtTokenProvider {
    private final ObjectMapper objectMapper;
    private final ConstJwt constJwt;
    private final SecretKey secretKey;

    public JwtTokenProvider(ObjectMapper objectMapper, ConstJwt constJwt) {
        this.objectMapper = objectMapper;
        this.constJwt = constJwt;
        // 시크릿 키 문자열을 암호화에 쓸 수 있는 키 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(constJwt.secretKey()));

        log.info("constJwt: {}", this.constJwt);
    }

    // Access Token 생성. 만료되면 Refresh Token으로 재발급 받아야 함
    public String generateAccessToken(JwtMember jwtMember){
        return generateToken(jwtMember , constJwt.accessTokenValidityMilliseconds());
    }

    // Refresh Token 생성. AT가 만료됐을 때 새 AT를 발급받기 위한 토큰. RT가 없거나 만료되면 다시 로그인해야 함
    public String generateRefreshToken(JwtMember jwtMember){
        return generateToken(jwtMember , constJwt.refreshTokenValidityMilliseconds());
    }

    // JWT 문자열 만드는 메소드. 암호화된 문자열(데이터, 토큰만료 시간 포함)
    public String generateToken(JwtMember jwtMember, long tokenValidityMilleSeconds){
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(tokenValidityMilleSeconds);

        log.info("now: {}, expiryDate: {}", now, expiryDate);
        return Jwts.builder()
                // [헤더] 토큰 타입을 Bearer로 지정
                .header().type(constJwt.bearerFormat())
                .and()
                // [페이로드] 발급자
                .issuer(constJwt.issuer())
                // [페이로드] 발급 시간
                .issuedAt(Date.from(now))
                // [페이로드] 만료 시간 (현재시간 + 전달받은 유효시간)
                .expiration(Date.from(expiryDate))
                // [페이로드] 실제 유저 데이터 (jwtMember 값)
                .claim(constJwt.claimKey(), makeClaimMyUserToJson(jwtMember))
                // 시크릿 키로 서명 (위변조 방지)
                .signWith(secretKey)
                .compact();
    }

    // JwtMember 객체를 JSON 문자열로 변환해서 토큰 payload에 담기
    public String makeClaimMyUserToJson(JwtMember jwtMember){
        return objectMapper.writeValueAsString(jwtMember);
    }

    // 토큰 문자열에서 jwtMember 객체 꺼내기
    public JwtMember getJwtMemberFromToken(String token){
        Claims claims = getClaims(token); // 토큰 검증 후 payload 전체 꺼내기

        //loginMember 키 값으로 담겨져 있는 value를 String타입으로 리턴
        String json = claims.get(constJwt.claimKey(), String.class);

        //JSON > Object. json문자열을 JwtMember 객체로 변환
        return objectMapper.readValue(json, JwtMember.class);
    }

    // 토큰 서명 검증 후 payload(Claims) 반환하는 내부 메서드
    // 토큰이 위변조됐거나 만료됐으면 여기서 예외 발생
    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)  // 시크릿 키로 서명 검증
                .build()
                .parseSignedClaims(token)  // 토큰 파싱 (만료/위변조시 예외 발생)
                .getPayload();  // payload(Claims) 꺼내기
    }
}