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
    private static final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";

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
     * @param queue
     * @param userId
     * @return
     */
    public Mono<Boolean> isAllowed(final String queue, final Long userId){
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_PROCEED_KEY.formatted(queue), userId.toString())
            .defaultIfEmpty(-1L)
            .map(rank-> rank >= 0);
    }

}
