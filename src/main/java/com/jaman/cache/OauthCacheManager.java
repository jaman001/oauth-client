package com.jaman.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OauthCacheManager<K, V> {

    private final Cache<K, V> cache = Caffeine.newBuilder()
                                              .expireAfter(new Expiry<K, V>() {
                                                  public long expireAfterCreate(K key, V value, long currentTime) {
                                                      if (key.toString().contains("internal")) {
                                                          return TimeUnit.HOURS.toNanos(24);// 24 hours (milliseconds)
                                                      } else {
                                                          return TimeUnit.MINUTES.toNanos(15);// 15 mins (milliseconds)
                                                      }

                                                  }

                                                  @Override
                                                  public long expireAfterUpdate(K key, V value, long currentTime,
                                                                                @NonNegative long currentDuration) {
                                                      return currentDuration;
                                                  }

                                                  @Override
                                                  public long expireAfterRead(K key, V value, long currentTime,
                                                                              @NonNegative long currentDuration) {
                                                      return currentDuration;
                                                  }
                                              })
                                              .maximumSize(10000L)
                                              .evictionListener((key, item, cause) -> log.info(
                                               this.getClass().getName().concat(": key {} was evicted "), key))
                                              .removalListener((key, item, cause) -> log.info(
                                               this.getClass().getName().concat(": Key {} was removed "), key))
                                              .scheduler(Scheduler.systemScheduler())
                                              .build();

    public Mono<V> get(K key, Mono<V> handler) {
        return Mono.justOrEmpty(this.cache.getIfPresent(key))
                   .switchIfEmpty(Mono.defer(() -> handler.flatMap(it -> this.put(key, it))));
    }

    public Mono<V> put(K key, V object) {
        this.cache.put(key, object);
        return Mono.just(object);
    }

    public void remove(K key) {
        this.cache.invalidate(key);
    }
}
