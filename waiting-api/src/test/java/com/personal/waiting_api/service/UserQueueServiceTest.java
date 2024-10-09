package com.personal.waiting_api.service;

import static org.junit.jupiter.api.Assertions.*;

import com.personal.waiting_api.EmbeddedRedis;
import com.personal.waiting_api.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
class UserQueueServiceTest {

    @Autowired
    private UserQueueService userQueueService;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @BeforeEach // 각 테스트 실행 전 실행될 로직
    public void beforeEach() {
        ReactiveRedisConnection connection = reactiveRedisTemplate.getConnectionFactory()
            .getReactiveConnection();
        connection.serverCommands().flushAll().subscribe(); // redis 초기화
    }

    @Test
    void registerWaitQueue() {
        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 100L))
            .expectNext(1L)
            .verifyComplete();

        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 101L))
            .expectNext(2L)
            .verifyComplete();

        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 102L))
            .expectNext(3L)
            .verifyComplete();
    }

    @Test
    void alreadyRegisterWaitQueue() {
        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 100L))
            .expectNext(1L)
            .verifyComplete();

        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 100L))
            .expectError(ApplicationException.class)
            .verify();
    }

    @Test
    void allowUser() {
        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 100L)
                .then(userQueueService.registerWaitQueue("default", 101L))
                .then(userQueueService.registerWaitQueue("default", 102L))
                .then(userQueueService.allowUser("default", 2L))
            ).expectNext(2L)
            .verifyComplete();
    }

    @Test
    void allowUser2() {
        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 100L)
                .then(userQueueService.registerWaitQueue("default", 101L))
                .then(userQueueService.registerWaitQueue("default", 102L))
                .then(userQueueService.allowUser("default", 5L))
            ).expectNext(3L)
            .verifyComplete();
    }

    @Test
    void allowUser3() {
        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 100L)
                .then(userQueueService.registerWaitQueue("default", 101L))
                .then(userQueueService.registerWaitQueue("default", 102L))
                .then(userQueueService.allowUser("default", 5L))
                .then(userQueueService.registerWaitQueue("default", 200L))
            ).expectNext(1L)
            .verifyComplete();
    }

    @Test
    void isAllowed() {
        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 100L)
                .then(userQueueService.allowUser("default", 1L))
                .then(userQueueService.isAllowed("default", 100L)))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void getRankNumber() {
        StepVerifier
            .create(userQueueService.registerWaitQueue("default", 100L)
                .then(userQueueService.registerWaitQueue("default", 101L))
                .then(userQueueService.registerWaitQueue("default", 102L))
                .then(userQueueService.getRankNumber("default", 102L)))
            .expectNext(3L)
            .verifyComplete();

        StepVerifier.create(userQueueService.getRankNumber("default", 100L))
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    void emptyRank(){
        StepVerifier.create(userQueueService.getRankNumber("default", 100L))
            .expectNext(-1L)
            .verifyComplete();
    }

    @Test
    void isAllowedByToken() {
        StepVerifier.create(userQueueService.isAllowedByToken("default", 100L, "d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8"))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void isNotAllowedByToken() {
        StepVerifier.create(userQueueService.isAllowedByToken("default", 100L, ""))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void generateToken(){
        StepVerifier.create(userQueueService.generateToken("default", 100L))
            .expectNext("d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8")
            .verifyComplete();
    }
}