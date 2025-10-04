package com.USWCicrcleLink.server.global.security.jwt;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.JwtException;
import com.USWCicrcleLink.server.global.exception.errortype.TokenException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.global.security.details.service.UserDetailsServiceManager;
import com.USWCicrcleLink.server.global.security.jwt.domain.TokenValidationResult;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JWT를 생성하고 검증
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1800000L; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 604800000L; // 7일
    private final UserDetailsServiceManager userDetailsServiceManager;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret.key}")
    private String secretKeyString;
    private Key secretKey;

    @Value("${security.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${security.cookie.same-site:Lax}")
    private String cookieSameSite;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 엑세스 토큰 생성 및 응답 헤더에 추가
     */
    public String createAccessToken(UUID uuid, HttpServletResponse response) {
        UserDetails userDetails = userDetailsServiceManager.loadUserByUuid(uuid);

        Claims claims = Jwts.claims().setSubject(uuid.toString());

        UUID clubUUID = null;
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            clubUUID = customUserDetails.getClubUUIDs().isEmpty() ? null : customUserDetails.getClubUUIDs().get(0);
        } else if (userDetails instanceof CustomLeaderDetails customLeaderDetails) {
            clubUUID = customLeaderDetails.getClubUUID();
        }

        if (clubUUID != null) {
            claims.put("clubUUID", clubUUID.toString());
        }

        Date now = new Date();
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        response.setHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
        return accessToken;
    }

    /**
     * 엑세스 토큰에서 사용자 정보 추출
     */
    public UserDetails getUserDetails(String accessToken) {
        UUID uuid = getUUIDFromAccessToken(accessToken);
        return userDetailsServiceManager.loadUserByUuid(uuid);
    }

    /**
     * 액세스 토큰 유효성 검증
     */
    public TokenValidationResult validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return TokenValidationResult.INVALID; // 비어있는 토큰
        }

        try {
            Claims claims = getClaims(accessToken);

            if (claims == null) {
                return TokenValidationResult.INVALID; // claims null
            }

            return claims.getExpiration().before(new Date()) ? TokenValidationResult.EXPIRED : TokenValidationResult.VALID;

        } catch (ExpiredJwtException e) {
            return TokenValidationResult.EXPIRED; // 만료된 토큰
        } catch (MalformedJwtException e) {
            return TokenValidationResult.INVALID; // 토큰이 변조되었거나 잘못된 형식
        } catch (SignatureException e) {
            return TokenValidationResult.INVALID; // 서명이 맞지 않음 (변조 가능성)
        } catch (JwtException | IllegalArgumentException e) {
            return TokenValidationResult.INVALID; // 기타 JWT 오류
        }
    }

    /**
     * 엑세스 토큰에서 UUID 추출
     */
    private UUID getUUIDFromAccessToken(String accessToken) {
        String uuidStr = getClaims(accessToken).getSubject();
        return UUID.fromString(uuidStr);
    }

    // JWT Claims 파싱 및 반환
    private Claims getClaims(String jwtToken) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    /**
     * 헤더에서 엑세스 토큰 추출
     */
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 엑세스 토큰에서 인증 정보 가져오기
     */
    public Authentication getAuthentication(String accessToken) {
        UserDetails userDetails = getUserDetails(accessToken);

        List<GrantedAuthority> authorities = userDetails.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    /**
     * 리프레시 토큰 생성
     */
    public String createRefreshToken(UUID uuid, HttpServletResponse response) {
        deleteRefreshToken(uuid);

        String newRefreshToken = UUID.randomUUID().toString();
        String redisKey = "refreshToken:" + newRefreshToken;

        redisTemplate.opsForValue().set(redisKey, uuid.toString(), REFRESH_TOKEN_EXPIRATION_TIME, TimeUnit.MILLISECONDS);

        setRefreshTokenCookie(response, newRefreshToken);
        log.debug("새로운 Refresh Token 발급 - UUID: {}", uuid);
        return newRefreshToken;
    }

    /**
     * 리프레시 토큰 검증 (Redis 조회 방식)
     */
    public void validateRefreshToken(String refreshToken, HttpServletRequest request) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new TokenException(ExceptionType.INVALID_TOKEN);
        }

        boolean existsInRedis = redisTemplate.opsForValue().get("refreshToken:" + refreshToken) != null;
        if (!existsInRedis) {
            String clientIp = request.getRemoteAddr();
            String requestUri = request.getRequestURI();
            log.warn("Refresh Token 검증 실패 - | IP: {} | 요청 경로: {}", clientIp, requestUri);
            throw new TokenException(ExceptionType.INVALID_TOKEN);
        }

        log.debug("Refresh Token 검증 성공");
    }

    /**
     * 리프레시 토큰에서 UUID 추출 (Redis에서 조회)
     */
    public UUID getUUIDFromRefreshToken(String refreshToken) {
        String storedUuid = redisTemplate.opsForValue().get("refreshToken:" + refreshToken);
        if (storedUuid == null) {
            throw new UserException(ExceptionType.INVALID_TOKEN);
        }
        return UUID.fromString(storedUuid);
    }

    /**
     * 리프레시 토큰 삭제
     */
    public void deleteRefreshToken(UUID uuid) {
        log.debug("리프레시 토큰 삭제 진행 - UUID: {}", uuid);

        // 기존 Refresh Token 조회
        Set<String> keys = redisTemplate.keys("refreshToken:*");
        if (keys != null) {
            for (String key : keys) {
                String storedUuid = redisTemplate.opsForValue().get(key);
                if (uuid.toString().equals(storedUuid)) {
                    redisTemplate.delete(key);
                    log.debug("기존 Refresh Token 삭제 완료 - Key: {}", key);
                    break;
                }
            }
        }
    }

    /**
     * 쿠키에서 리프레시 토큰 추출
     */
    public String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 리프레시 토큰을 HttpOnly 쿠키로 설정 (SameSite/ Secure 프로퍼티 반영)
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        int maxAge = (int) (REFRESH_TOKEN_EXPIRATION_TIME / 1000);
        String sameSite = (cookieSameSite == null || cookieSameSite.isBlank()) ? "Lax" : cookieSameSite;
        String securePart = cookieSecure ? "; Secure" : "";
        String cookieValue = String.format("refreshToken=%s; Path=/; HttpOnly; Max-Age=%d; SameSite=%s%s",
                refreshToken, maxAge, sameSite, securePart);
        response.setHeader("Set-Cookie", cookieValue);
    }

    /**
     * 쿠키에서 리프레시 토큰 삭제 (SameSite/ Secure 프로퍼티 반영)
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        String sameSite = (cookieSameSite == null || cookieSameSite.isBlank()) ? "Lax" : cookieSameSite;
        String securePart = cookieSecure ? "; Secure" : "";
        String cookieValue = String.format("refreshToken=; Path=/; HttpOnly; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=%s%s",
                sameSite, securePart);
        response.setHeader("Set-Cookie", cookieValue);
        log.debug("클라이언트 쿠키에서 리프레시 토큰 삭제 완료");
    }
}