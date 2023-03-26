package org.animeatsume;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;

@Log4j2
@Configuration
@EnableAsync
@EnableCaching
@EnableScheduling
public class ApplicationConfig {
    private static final long WEEK_IN_MILLISECONDS = 1000 * 60 * 60 * 24 * 7;
    private static final long ANIME_TITLE_SEARCH_CACHE_CLEAR_INTERVAL = WEEK_IN_MILLISECONDS;

//    @Value("${org.animeatsume.cache.cache-names}")
    public static String[] CACHE_NAMES;
//    @Value("${org.animeatsume.cache.anime-title-search}")
    public static String ANIME_TITLE_SEARCH_CACHE_NAME;

    public ApplicationConfig(
        @Value("${org.animeatsume.cache.cache-names}") String[] CACHE_NAMES,
        @Value("${org.animeatsume.cache.anime-title-search}") String ANIME_TITLE_SEARCH_CACHE_NAME
    ) {
        ApplicationConfig.CACHE_NAMES = CACHE_NAMES;
        ApplicationConfig.ANIME_TITLE_SEARCH_CACHE_NAME = ANIME_TITLE_SEARCH_CACHE_NAME;
        log.info("CACHE_NAMES: {}", CACHE_NAMES);
        log.info("ANIME_TITLE_SEARCH_CACHE_NAME: {}", ANIME_TITLE_SEARCH_CACHE_NAME);
    }

    @Bean
    public Executor taskExecutor(
        @Value("${spring.task.execution.pool.core-size}") int corePoolSize,
        @Value("${spring.task.execution.pool.max-size}") int maxPoolSize,
        @Value("${spring.task.execution.pool.queue-capacity}") int queueCapacity,
        @Value("${spring.task.execution.thread-name-prefix}") String threadNamePrefix
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();

        return executor;
    }

    // Note: We can't use `@Autowired CacheManager cacheManager` because the cache names array needs to be final,
    // and the only way to do that when mixing with the CacheManager is via manual `@Bean` method.
    // See:
    //  - https://www.javadevjournal.com/spring-boot/3-ways-to-configure-multiple-cache-managers-in-spring-boot/
    @Bean
    public CacheManager cacheManager(@Value("${org.animeatsume.cache.cache-names}") String[] cacheNames) {
        log.info("Caching activated for cache names: {}", cacheNames);

        return new ConcurrentMapCacheManager(cacheNames);
    }

    public CacheManager cacheManager() {
        return cacheManager(null);
    }

    // Cannot use runtime vars like those populated by `@Value()` inside `@CacheEvict` annotation
    // nor `@Scheduled` annotations, so the string reference inside via `CacheManager` constructor
    // call will technically be null, but for now that's ok since we have no other caches.
    //@CacheEvict(allEntries = true, value = ANIME_TITLE_SEARCH_CACHE_NAME)
    @Scheduled(fixedDelay = ANIME_TITLE_SEARCH_CACHE_CLEAR_INTERVAL)
    public void clearSearchAnimeTitleCache() {
        clearCache(ANIME_TITLE_SEARCH_CACHE_NAME);
    }

    private void clearCache() {
        CacheManager cacheManager = cacheManager(CACHE_NAMES);

        cacheManager.getCacheNames().forEach(this::clearCache);
    }

    private void clearCache(String cacheName) {
        CacheManager cacheManager = cacheManager(CACHE_NAMES);

        if (cacheName != null && cacheName.length() > 0) {
            try {
                clearCache(cacheManager, cacheName);
            } catch (Exception e) {
                log.error("Cache name ({}) either doesn't exist or couldn't be cleared", cacheName);
            }
        } else {
            // Default to clearing all caches
            clearCache();
        }
    }

    private void clearCache(CacheManager cacheManager, String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {
            cache.clear();

            log.info("Cleared cache ({}) at {}", cache.getName(), LocalDateTime.now());
        }
    }
}
