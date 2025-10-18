package com.openstep.balllinkbe.global.config.websocket;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class IdempotencyCache {

    private static final long TTL_SECONDS = 60;
    private final Map<String, CacheItem> cache = new HashMap<>();

    public boolean seen(String key) {
        cleanup();
        return cache.containsKey(key);
    }

    public Optional<Map<String, Object>> get(String key) {
        cleanup();
        CacheItem item = cache.get(key);
        if (item == null || Instant.now().isAfter(item.expiry)) return Optional.empty();
        return Optional.of(item.value);
    }

    public void put(String key, Map<String, Object> value) {
        cache.put(key, new CacheItem(value, Instant.now().plusSeconds(TTL_SECONDS)));
    }

    private void cleanup() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(e -> now.isAfter(e.getValue().expiry));
    }

    private record CacheItem(Map<String, Object> value, Instant expiry) {}
}
