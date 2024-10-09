package com.personal.web_api;

import com.personal.web_api.dto.AllowedUserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@SpringBootApplication
public class WebsiteApplication {

    RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(WebsiteApplication.class, args);
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "queue", defaultValue = "default") String queue,
        @RequestParam("user_id") Long userId, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String cookieKey = "users-queue-%s-token".formatted(queue);

        String token = "";
        if(cookies != null){
            Optional<Cookie> cookie = Arrays.stream(cookies)
                .filter(i -> i.getName().equalsIgnoreCase(cookieKey))
                .findFirst();
            token = cookie.orElse(new Cookie(cookieKey, "")).getValue();
        }

        URI uri = UriComponentsBuilder
            .fromHttpUrl("http://127.0.0.1:9010")
            .path("/api/v1/queue/allowed")
            .queryParam("queue", queue)
            .queryParam("user_id", userId)
            .queryParam("token", token)
            .encode()
            .build()
            .toUri();
        ResponseEntity<AllowedUserResponse> response = restTemplate.getForEntity(uri,
            AllowedUserResponse.class);
        if (response.getBody() == null || !response.getBody().isAllowed()) {
            return "redirect:http://127.0.0.1:9010/waiting-room?queue=%s&user_id=%d&redirect_url=%s"
                .formatted(queue, userId,
                    "http://127.0.0.1:9000?user_id=%d".formatted(userId));
        }
        return "index";
    }

}
