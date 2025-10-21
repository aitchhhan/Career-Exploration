package career.exploration.config;



import career.exploration.security.CustomAuthorizationRequestResolver;
import career.exploration.security.JwtAuthenticationFilter;
import career.exploration.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults()) // cors 등록
                .httpBasic(AbstractHttpConfigurer::disable) // Spring Security의 기본 인증 방식인 Basic Authentication을 비활성화
                .csrf(AbstractHttpConfigurer::disable) // JWT는 CSRF 공격에 취약하지 않아 CSRF 보호 비활성화 // 보통 CSRF 보호는 세션 기반 인증을 위해 사용
                .formLogin(AbstractHttpConfigurer::disable) //  Spring Security의 기본 폼 로그인 기능을 비활성화
                // JWT를 사용하기 때문에 세션을 사용하지 않도록 STATELESS로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestResolver(customAuthorizationRequestResolver)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                )
                // Spring Security의 UsernamePasswordAuthenticationFilter 실행 전에 JwtAuthenticationFilter를 실행하도록 설정하여 모든 요청에서 JWT 검증이 이루어지고, 유효한 JWT면 인증 정보를 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // 설정이 완료된 HttpSecurity 객체를 빌드하여 SecurityFilterChain을 반환함으로써 해당 설정이 Spring Security의 보안 필터로 동작하도록 함
                            // 이제 Spring Security는 JWT 인증을 적용한 상태로 모든 요청을 처리함.
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "https://domain.com")); // 허용할 Origin(요청 출처) 지정
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드 지정
        config.setAllowedHeaders(List.of("*")); // 허용할 요청 헤더 지정 // "*"로 지정하면 모든 헤더를 허용함
        config.setAllowCredentials(true); // true로 설정하면 클라이언트에서 쿠키 포함 요청이 가능

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // URL 패턴에 따라 위의 CORS 설정을 등록할 객체 생성
        source.registerCorsConfiguration("/**", config); // 모든 엔드포인트에 대해 위에서 정의한 CORS 설정 적용
        return source; // Spring Security에서 참조할 CORS 설정 소스 반환
    }
}