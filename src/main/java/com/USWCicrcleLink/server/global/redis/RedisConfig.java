package com.USWCicrcleLink.server.global.redis;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.user.domain.SignupToken;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create("redis://" +redisHost + ":" + redisPort);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, Integer.parseInt(redisPort));
        config.setDatabase(0); // 기본 DB 0 사용
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisConnectionFactory bucketRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, Integer.parseInt(redisPort));
        config.setDatabase(1); // bucket 전용 DB 1 사용
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisConnectionFactory emailTokenRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, Integer.parseInt(redisPort));
        config.setDatabase(2); // EmailToken 전용 DB 2번
        return new LettuceConnectionFactory(config);
    }
    @Bean
    public RedisConnectionFactory signUpTokenRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, Integer.parseInt(redisPort));
        config.setDatabase(3); //SignupToken 전용 DB 3번
        return new LettuceConnectionFactory(config);
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }


    @Bean(name = "bucketRedisTemplate")
    public RedisTemplate<String, Object> bucketRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        return redisTemplate;
    }

    @Bean(name = "emailTokenRedisTemplate")
    public RedisTemplate<String, EmailToken> emailTokenRedisTemplate() {
        RedisTemplate<String, EmailToken> template = new RedisTemplate<>();
        template.setConnectionFactory(emailTokenRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(EmailToken.class));
        return template;
    }

    @Bean(name = "singUpTokenRedisTemplate")
    public RedisTemplate<String, SignupToken> signUpTokenRedisTemplate() {
        RedisTemplate<String, SignupToken> template = new RedisTemplate<>();
        template.setConnectionFactory(signUpTokenRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(SignupToken.class));
        return template;
    }


}
