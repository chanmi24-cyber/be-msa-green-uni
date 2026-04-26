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

    // 15분 만료 토큰 생성
    public String generateAccessToken(JwtMember jwtMember){
        return generateToken(jwtMember , constJwt.accessTokenValidityMilliseconds());
    }

    // 15일 만료 토큰 생성
    public String generateRefreshToken(JwtMember jwtMember){
        return generateToken(jwtMember , constJwt.refreshTokenValidityMilliseconds());
    }

    // JWT 문자열 만드는 메소드. 암호화된 문자열(데이터, 토큰만료 시간 포함)
    public String generateToken(JwtMember jwtMember, long tokenValidityMilleSeconds){
        Date now = new Date();
        return Jwts.builder()
                // header
                .header().type(constJwt.bearerFormat())
                .and()
                //payload
                .issuer(constJwt.issuer()) // 발급자
                .issuedAt(now) // 토큰 발급 시간
                .expiration(new Date(now.getTime() + tokenValidityMilleSeconds)) // 토큰 만료 시간
                .claim(constJwt.claimKey(), makeClaimMyUserToJson(jwtMember)) // 로그인 유저 실제 데이터
                /// key(loginMember) , value(JwtMember객체)
                .signWith(secretKey)
                .compact();
    }

    // 토큰 안에 jwtMember 데이터 넣기
    public String makeClaimMyUserToJson(JwtMember jwtMember){
        return objectMapper.writeValueAsString(jwtMember); // jwtMember 객체를 JSON 문자열로 변환하여 담음
    }

    // 토큰 안에 jwtMember 데이터 꺼내기
    public JwtMember getJwtMemberFromToken(String token){
        Claims claims = getClaims(token); // 토큰 검정, 내용 꺼내기

        //loginMember 키 값으로 담겨져 있는 value를 String타입으로 리턴
        String json = claims.get(constJwt.claimKey(), String.class);

        //JSON > Object. json문자열을 JwtMember 객체로 변환
        return objectMapper.readValue(json, JwtMember.class);
    }

    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
