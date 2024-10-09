package com.personal.waiting_api;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class EmbeddedRedis {

    private Logger logger = LoggerFactory.getLogger(EmbeddedRedis.class);

    private final RedisServer redisServer;

    public EmbeddedRedis() throws IOException {
        this.redisServer = new RedisServer(63790);
    }

    @PostConstruct
    public void start() throws IOException {
        this.redisServer.start();
        logger.info("redis-server started...");
    }

    @PreDestroy
    public void stop() throws IOException {
        this.redisServer.stop();
        logger.info("redis-server ended...");
    }

}
