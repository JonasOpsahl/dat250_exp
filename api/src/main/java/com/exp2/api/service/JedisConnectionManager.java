package com.exp2.api.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import redis.clients.jedis.UnifiedJedis;

@Component
public class JedisConnectionManager {

    private final UnifiedJedis jedis;

    public JedisConnectionManager() {
        String redisHost = "localhost";
        int redisPort = 6379;
        String redisUrl = String.format("redis://%s:%d", redisHost, redisPort);
        this.jedis = new UnifiedJedis(redisUrl);
    }

    public UnifiedJedis getJedis() {
        return this.jedis;
    }

    @PreDestroy
    public void closeConnection() {
        if (this.jedis != null) {
            System.out.println("Closing connection");
            this.jedis.close();
        }
    }
}