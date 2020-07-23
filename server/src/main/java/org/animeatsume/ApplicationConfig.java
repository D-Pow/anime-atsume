package org.animeatsume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableCaching
@EnableScheduling
public class ApplicationConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final long WEEK_IN_MILLISECONDS = 1000 * 60 * 60 * 24 * 7;
    private static final long KISSANIME_TITLE_SEARCH_CACHE_CLEAR_INTERVAL = WEEK_IN_MILLISECONDS;

    public static final String KISSANIME_TITLE_SEARCH_CACHE_NAME = "kissanimeTitleSearch";

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

    @Bean
    public CacheManager cacheManager(@Value("${org.animeatsume.cache.cache-names}") String[] cacheNames) {
        log.info("Caching activated for cache names: {}", Arrays.asList(cacheNames));

        return new ConcurrentMapCacheManager(cacheNames);
    }

    @CacheEvict(allEntries = true, value = KISSANIME_TITLE_SEARCH_CACHE_NAME)
    @Scheduled(fixedDelay = KISSANIME_TITLE_SEARCH_CACHE_CLEAR_INTERVAL)
    public void clearKissanimeTitleCache() {
        log.info("Cleared Kissanime-title-search cache at {}", LocalDateTime.now());
    }
}
