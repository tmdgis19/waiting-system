package com.personal.flow.controller;

import com.personal.flow.service.UserQueueService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {

    private final UserQueueService userQueueService;

    @GetMapping("/waiting-room")
    Mono<Rendering> waitingRoomPage(
        @RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam(name = "user_id") Long userId,
        @RequestParam(name = "redirect_url") String redirectURL,
        ServerWebExchange exchange) {
        // 1. 입장이 허용되어 페이지 redirect가 가능한 상태인가?
        // 2. 어디로 이동해야 하는가?
        String key = "users-queue-%s-token".formatted(queue);
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(key);
        String token = cookie == null ? "" : cookie.getValue();
        return userQueueService.isAllowedByToken(queue, userId, token)
            .filter(allowed -> allowed)
            .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectURL).build()))
            .switchIfEmpty( // 허용 여부가 true인 것만 필터링 했기 때문에 아니라면 빈 값이 넘어옴
                userQueueService.registerWaitQueue(queue, userId)
                    .onErrorResume(ex -> userQueueService.getRankNumber(queue, userId))
                    .map(rank -> Rendering.view("waiting-room.html")
                        .modelAttribute("number", rank)
                        .modelAttribute("user_id", userId)
                        .modelAttribute("queue", queue)
                        .build()
                    )
            );
    }

}
