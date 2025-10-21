package career.exploration.security;


import career.exploration.domain.Member;
import career.exploration.enums.RoleType;
import career.exploration.exception.EmptyMemberException;
import career.exploration.exception.HandleJwtException;
import career.exploration.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = jwtUtility.extractJwtFromCookies(request);

        if (jwt != null) {
            try {
                if (jwtUtility.validateJwt(jwt)) {
                    Authentication auth = getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (HandleJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Authentication getAuthentication(String jwt) {
        Claims claims = jwtUtility.getClaimsFromJwt(jwt);
        Member member = memberRepository.findByEmail(claims.getSubject()).orElseThrow(EmptyMemberException::new);
        RoleType roleType = RoleType.valueOf(claims.get("role", String.class));

        return new UsernamePasswordAuthenticationToken(
                member,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleType.name()))
        );
    }
}
