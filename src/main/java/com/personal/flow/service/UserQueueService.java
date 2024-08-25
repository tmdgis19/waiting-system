package com.personal.flow.service;

import com.personal.flow.exception.ApplicationException;
import com.personal.flow.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private static final String WAITING_QUEUE_KEY = "users:queue:%s:wait";

    private static final String WAITING_QUEUE_KEY_FOR_SCAN = "users:queue:*:wait";
    private static final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";

    @Value("${scheduler.enabled}")
    private Boolean scheduled;

    /**
     * 대기열 등록 API
     *
     * @param userId
     * @return 등록한 유저의 rank(정렬된 순서)
     */
    public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
        //redis sorted set
        // zadd key score member

        long unixTimeStamp = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet()
            .add(WAITING_QUEUE_KEY.formatted(queue), userId.toString(), unixTimeStamp)
            .filter(result -> result)
            .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build()))
            .flatMap(
                i -> reactiveRedisTemplate.opsForZSet()
                    .rank(WAITING_QUEUE_KEY.formatted(queue), userId.toString()))
            .map(rank -> rank >= 0 ? rank + 1 : rank);
    }

    // 진입이 가능한 상태인지 조회
    // 진입을 허용
    public Mono<Long> allowUser(final String queue, final Long count) {
        long unixTimeStamp = Instant.now().getEpochSecond();

        return reactiveRedisTemplate.opsForZSet().popMin(WAITING_QUEUE_KEY.formatted(queue), count)
            .flatMap(member -> reactiveRedisTemplate.opsForZSet()
                .add(USER_QUEUE_PROCEED_KEY.formatted(queue), member.getValue(), unixTimeStamp))
            .count();
    }

    /**
     * 진입이 허용된 상태의 유저인지 확인
     *
     * @param queue
     * @param userId
     * @return
     */
    public Mono<Boolean> isAllowed(final String queue, final Long userId) {
        return reactiveRedisTemplate.opsForZSet()
            .rank(USER_QUEUE_PROCEED_KEY.formatted(queue), userId.toString())
            .defaultIfEmpty(-1L)
            .map(rank -> rank >= 0);
    }

    public Mono<Boolean> isAllowedByToken(final String queue, Long userId, final String token) {
        return generateToken(queue, userId)
            .filter(gen-> gen.equalsIgnoreCase(token))
            .map(i-> true)
            .defaultIfEmpty(false);
    }

    public Mono<Long> getRankNumber(String queue, Long userId) {
        return reactiveRedisTemplate.opsForZSet()
            .rank(WAITING_QUEUE_KEY.formatted(queue), userId.toString())
            .defaultIfEmpty(-1L)
            .map(rank -> rank >= 0 ? rank + 1 : rank);
    }

    public Mono<String> generateToken(String queue, Long userId) {
        // sha256
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = "user-queue-%s-%d".formatted(queue, userId);
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte aByte : encodedHash) {
                hexString.append(String.format("%02x", aByte));
            }
            return Mono.just(hexString.toString());
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public void scheduleAllowUser() {
        if(!scheduled){
            log.info("passed scheduling...");
            return;
        }

        log.info("called scheduling...");

        long maxAllowUserCount = 100L;
        reactiveRedisTemplate
            .scan(ScanOptions.scanOptions()
                .match(WAITING_QUEUE_KEY_FOR_SCAN)
                .count(100)
                .build())
            .map(key -> key.split(":")[2])
            .flatMap(queue -> allowUser(queue, maxAllowUserCount)
                .map(allowCount -> Tuples.of(queue, allowCount)))
            .doOnNext(
                tuple -> log.info("Tried {} and allowed {} members of {} queue", maxAllowUserCount,
                    tuple.getT2(), tuple.getT1()))
            .subscribe();
    }

}
