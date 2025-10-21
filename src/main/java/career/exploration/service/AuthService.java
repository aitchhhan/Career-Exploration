package career.exploration.service;

import career.exploration.domain.Member;
import career.exploration.exception.EmptyMemberException;
import career.exploration.exception.InvalidJwtException;
import career.exploration.repository.MemberRepository;
import career.exploration.security.JwtUtility;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtility jwtUtility;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cookie.secure}")
    private boolean isSecure;

    @Value("${cookie.sameSite}")
    private String isSameSite;

    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies(); // 클라이언트가 보낸 요청에서 쿠키 추출
        String refreshToken = null; // refresh_token 저장할 변수 선언

        if (cookies != null) { // 쿠키가 없을 수도 있으니,,,
            for (Cookie cookie : cookies) { // 쿠키 배열을 하나씩 순회하며 Key가 "refresh_token"인 쿠키 찾기
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue(); // 쿠키에서 Key가 "refresh_token"인 Value 추출
                    break;
                }
            }
        }

        if (refreshToken == null) { // refresh_token 없는데 요청한 경우 메시지와 401 반환
            throw new InvalidJwtException("Refresh Token 없음");
        }

        // Redis 검증 후 새로운 access_token 발급
        Claims claims = jwtUtility.getClaimsFromJwt("Bearer " + refreshToken);
        String email = claims.getSubject();

        // Redis에 저장된 refresh_token 확인
        String savedRefreshToken = redisTemplate.opsForValue().get("refresh:" + email);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new InvalidJwtException("Invalid Refresh Token"); // 메시지와 함께 401 반환
        }
        // 새로운 access_token 발급
        Member member = memberRepository.findByEmail(email).orElseThrow(EmptyMemberException::new);
        String newAccessToken = jwtUtility.generateAccessJwt(member.getEmail(), member.getName(), member.getRoleType());

        ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite(isSameSite)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
    }
}
