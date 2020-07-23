package org.animeatsume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;

@Configuration
@EnableCaching
public class ApplicationConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

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
}
