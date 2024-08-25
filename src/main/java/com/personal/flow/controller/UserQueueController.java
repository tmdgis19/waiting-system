package com.personal.flow.controller;

import com.personal.flow.dto.AllowUserResponse;
import com.personal.flow.dto.AllowedUserResponse;
import com.personal.flow.dto.RankNumberResponse;
import com.personal.flow.dto.RegisterUserResponse;
import com.personal.flow.service.UserQueueService;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class UserQueueController {

    private final UserQueueService userQueueService;

    @PostMapping
    public Mono<RegisterUserResponse> registerUser(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam("user_id") Long userId) {
        return userQueueService.registerWaitQueue(queue, userId)
            .map(RegisterUserResponse::new);
    }

    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "count") Long count) {
        return userQueueService.allowUser(queue, count)
            .map(allowedCount -> new AllowUserResponse(count, allowedCount));
    }

    @GetMapping("/allowed")
    public Mono<AllowedUserResponse> isAllowedUser(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam("user_id") Long userId, @RequestParam("token") String token) {
        return userQueueService.isAllowedByToken(queue, userId, token)
            .map(isAllowed -> new AllowedUserResponse(userId, isAllowed));
    }

    @GetMapping("/rank")
    public Mono<RankNumberResponse> getRank(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId) {
        return userQueueService.getRankNumber(queue, userId)
            .map(RankNumberResponse::new);
    }

    @GetMapping("/touch")
    Mono<String> touch(@RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam("user_id") Long userId, ServerWebExchange exchange) {
        return Mono.defer(() -> userQueueService.generateToken(queue, userId))
            .map(token -> {
                exchange.getResponse().addCookie(
                    ResponseCookie.from("users-queue-%s-token".formatted(queue), token)
                        .path("/")
                        .maxAge(300)
                        .build()
                );
                return token;
            });
    }

}
