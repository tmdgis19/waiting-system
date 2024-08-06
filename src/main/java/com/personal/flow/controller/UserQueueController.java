package com.personal.flow.controller;

import com.personal.flow.dto.AllowUserResponse;
import com.personal.flow.dto.AllowedUserResponse;
import com.personal.flow.dto.RegisterUserResponse;
import com.personal.flow.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queue")
public class UserQueueController {

    private final UserQueueService userQueueService;

    @PostMapping
    public Mono<RegisterUserResponse> registerUser(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam("userId") Long userId) {
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
        @RequestParam("userId") Long userId){
        return userQueueService.isAllowed(queue, userId)
            .map(isAllowed-> new AllowedUserResponse(userId, isAllowed));
    }

}
