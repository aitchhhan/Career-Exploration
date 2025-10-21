package career.exploration.service;

import career.exploration.domain.Member;
import career.exploration.dto.LoginInfoRes;
import career.exploration.exception.EmptyMemberException;
import career.exploration.exception.InvalidLoginlException;
import career.exploration.repository.MemberRepository;
import career.exploration.security.JwtUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    @Value("${cookie.secure}")
    private boolean isSecure;

    @Value("${cookie.sameSite}")
    private String isSameSite;

    public LoginInfoRes getLoginStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidLoginlException("로그인이 안 되어 있음");
        }

        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(EmptyMemberException::new);

        return new LoginInfoRes(
                member.getName(),
                member.getEmail(),
                member.getRoleType()
        );
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, String email) {
        String redisKey = "refresh:" + email;
        redisTemplate.delete(redisKey);

        ResponseCookie deleteToken = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(isSecure)
                .sameSite(isSameSite)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteToken.toString());

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        ResponseCookie deleteJSession = ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteJSession.toString());

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}

