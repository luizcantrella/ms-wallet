package dev.cantrella.ms_wallet.infra.adapter.out.cache;

import dev.cantrella.ms_wallet.ports.out.CachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCacheAdapter implements CachePort {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> T get(String key, Class<T> type) {
        return type.cast(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
    }
}
