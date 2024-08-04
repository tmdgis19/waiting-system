package com.personal.flow.service;

import com.personal.flow.exception.ErrorCode;
import java.sql.Time;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private static final String WAITING_QUEUE_KEY = "users:queue:%s:wait";

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
                    .rank(WAITING_QUEUE_KEY.formatted(queue), userId.toString()));
    }

}
