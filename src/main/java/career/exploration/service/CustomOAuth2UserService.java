package career.exploration.service;


import career.exploration.domain.Member;
import career.exploration.repository.MemberRepository;
import career.exploration.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // DefaultOAuth2UserService는 Spring Security가 기본적으로 제공하는 OAuth2 사용자 정보 로드 서비스
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest); //Google API에서 사용자 정보를 가져와서 OAuth2User 객체로 반환

        // oAuth2User.getAttributes()를 호출하면, Google OAuth에서 받아온 사용자 정보가 Map<String, Object> 형태로 저장됨
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email"); // Object 타입을 String으로 Type Casting

        // 이메일 체크
        if (!email.endsWith("@sungkyul.ac.kr")) {
            // Spring Security에서 제공하는 내장 예외 클래스 // OAuth2 로그인 과정에서 발생하는 예외 처리용으로 사용
            throw new OAuth2AuthenticationException("성결대학교 구글 이메일로 로그인해주세요"); // Spring Security가 자동으로 감지해서 401 Unauthorized 응답 반환
        }

        // 사용자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new OAuth2AuthenticationException("접근 권한이 없는 유저입니다."));

        // OAuth2User 반환
        return new CustomOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(member.getRoleType().name())), // role이 하나라 singletonList 사용
                attributes);
    }
}
