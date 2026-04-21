package com.example.digitalwallet.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class RedisService {

     private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX
            = "blacklist:token:";
    private static final String IDEMPOTENCY_PREFIX
            = "idempotency:";
    private static final String WALLET_LOCK_PREFIX
            = "wallet:lock:";

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token , long ttlMilisec) {

        String key = BLACKLIST_PREFIX+token.hashCode();
        redisTemplate.opsForValue().set(key, token , Duration.ofMillis(ttlMilisec));
    }

    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX
                + token.hashCode();
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key));
    }

    public Boolean setIfAbsent(String refernceId , String value ,  Duration ttlMilisec) {

        String key = IDEMPOTENCY_PREFIX+refernceId;
        Boolean result =  redisTemplate.opsForValue().setIfAbsent(key , value,ttlMilisec);
        return Boolean.TRUE.equals(result);
    }

    public Boolean existIdempotencyKey(String referenceId){
        String key = IDEMPOTENCY_PREFIX+referenceId.hashCode();
        Boolean result =  redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    public boolean acquireLock(String walletId,
                               Duration ttl) {
        String key = WALLET_LOCK_PREFIX + walletId;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", ttl);

        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Lock acquired for wallet: {}",
                    walletId);
        }

        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(String walletId) {
        String key = WALLET_LOCK_PREFIX + walletId;
        redisTemplate.delete(key);
        log.debug("Lock released for wallet: {}", walletId);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key));
    }
}
