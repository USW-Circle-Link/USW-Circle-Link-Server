package com.USWCicrcleLink.server.global.bucket4j;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class APIRateLimiter {

    // API 요청에 대한 버킷을 생성하고 처리하는 로직을 담당하는 클래스
    private final LettuceBasedProxyManager<String> proxyManager;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    // redis 연결
    public APIRateLimiter(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        this.proxyManager = LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(65)))
                .build();
    }

    // 키에 해당하는 버킷을 가져오거나, 없을 경우 새로 생성하는 메서드
    private Bucket getOrCreateBucket(String bucketId, RateLimitAction action) {
        return buckets.computeIfAbsent(bucketId, key -> {
            BucketConfiguration configuration = createBucketConfiguration(action);
            return proxyManager.builder().build(key, configuration);
        });
    }

    // 버킷 설정을 생성하는 메서드
    private BucketConfiguration createBucketConfiguration(RateLimitAction action) {
        return BucketConfiguration.builder()
                .addLimit(action.getLimit())  // enum값에 따른 설정 가져오기
                .build();
    }

    // 키에 해당하는 버킷에서 토큰을 소비하려고 시도하는 메서드
    public boolean tryConsume(String bucketId, RateLimitAction action) {
        Bucket bucket = getOrCreateBucket(bucketId,action);
        return bucket.tryConsume(1);
    }

    // 버킷에 남은 토큰 수 확인
    public void logBucketState(String bucketId, RateLimitAction action) {
        Bucket bucket = getOrCreateBucket(bucketId, action);
        long availableTokens = bucket.getAvailableTokens();
        log.debug("버킷 아이디= {}, 이용가능한 토큰 수= {} ", bucketId, availableTokens);
    }
}
