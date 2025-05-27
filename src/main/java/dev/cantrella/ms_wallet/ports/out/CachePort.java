package dev.cantrella.ms_wallet.ports.out;

public interface CachePort {
    <T> T get(String key, Class<T> type);
    void put(String key, Object value);
    void evict(String key);
}
