package career.exploration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration // 스프링 설정 클래스를 의미
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.host}") String host, // application.yml에 있는 값을 주입 받아 Redis의 호스트와 포트 번호를 주입
            @Value("${spring.data.redis.port}") int port) {
        return new LettuceConnectionFactory(host, port); // LettuceConnectionFactory -> Redis와 연결하는 커넥션 팩토리
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>(); // RedisTemplate -> Spring이 제공하는 Redis 접근 도구
        template.setConnectionFactory(connectionFactory); // 앞에서 만든 LettuceConnectionFactory를 연결

        // Redis는 기본적으로 바이트 배열로 데이터 저장
        // 그래서 StringRedisSerializer를 설정해줘서 문자열로 직렬화 -> 직접 값을 확인할 때 사람이 읽을 수 있는 문자열로 보임
        template.setKeySerializer(new StringRedisSerializer()); // Key
        template.setValueSerializer(new StringRedisSerializer()); // Value
        template.setHashKeySerializer(new StringRedisSerializer()); // Hash의 Key
        template.setHashValueSerializer(new StringRedisSerializer()); // Hash의 Value

        return template;
    }

}

