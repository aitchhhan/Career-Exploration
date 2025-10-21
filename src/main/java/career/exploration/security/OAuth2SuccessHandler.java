package career.exploration.security;

import career.exploration.domain.Member;
import career.exploration.exception.EmptyMemberException;
import career.exploration.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtUtility jwtUtility;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cookie.secure}")
    private boolean isSecure;

    @Value("${cookie.sameSite}")
    private String isSameSite;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Member member = memberRepository.findByEmail(email).orElseThrow(EmptyMemberException::new);

        String accessToken = jwtUtility.generateAccessJwt(email, member.getName(), member.getRoleType());
        String refreshToken = jwtUtility.generateRefreshJwt(email);

        // Redis에 refresh token 저장 // key: refresh:email
        redisTemplate.opsForValue().set("refresh:" + email, refreshToken, 30, TimeUnit.DAYS);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite(isSameSite)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        // HttpOnly 쿠키로 refresh_token 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite(isSameSite)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        String redirectUrl = request.getParameter("state");

        // 유저가 로그인 시도하기 전에 요청했던 URL로 리디렉트
        response.sendRedirect(redirectUrl);
    }
}


