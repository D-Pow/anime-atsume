package org.animeatsume;

import lombok.extern.log4j.Log4j2;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

@Log4j2
@Configuration
@EnableAsync
@EnableCaching
@EnableScheduling
public class ApplicationConfig {
    private static final long WEEK_IN_MILLISECONDS = 1000 * 60 * 60 * 24 * 7;
    private static final long ANIME_TITLE_SEARCH_CACHE_CLEAR_INTERVAL = WEEK_IN_MILLISECONDS;

    @Value("${server.http.port}")
    private int SERVER_HTTP_PORT;

    @Value("${org.animeatsume.cache.cache-names}")
    public static String[] CACHE_NAMES;

    @Value("${org.animeatsume.cache.anime-title-search}")
    public static final String ANIME_TITLE_SEARCH_CACHE_NAME = "animeTitleSearch";

    @Value("${print.env}")
    private static Boolean logEnvVars;

    @Autowired
    ConfigurableEnvironment env;


    public ApplicationConfig(
        @Value("${org.animeatsume.cache.cache-names}") String[] CACHE_NAMES,
        @Value("${org.animeatsume.cache.anime-title-search}") String ANIME_TITLE_SEARCH_CACHE_NAME,
        @Value("${print.env}") Boolean logEnvVars
    ) {
        ApplicationConfig.CACHE_NAMES = CACHE_NAMES;
        // ApplicationConfig.ANIME_TITLE_SEARCH_CACHE_NAME = ANIME_TITLE_SEARCH_CACHE_NAME;
        ApplicationConfig.logEnvVars = logEnvVars;

        log.debug("CACHE_NAMES: {}", (Object[]) CACHE_NAMES);
        log.debug("ANIME_TITLE_SEARCH_CACHE_NAME: {}", ANIME_TITLE_SEARCH_CACHE_NAME);
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


    /**
     * @see <a href="https://stackoverflow.com/questions/30896234/how-set-up-spring-boot-to-run-https-http-ports/52648698#52648698">Correct Spring Boot v2 answer</a>
     * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config">Using separate .properties files per env</a>
     * @see <a href="https://stackoverflow.com/questions/44969873/spring-boot-key-store-password-set-in-code/44971126#44971126">Alternative: Setting server.ssl properties in Java code instead of .properties</a>
     * @see <a href="https://stackoverflow.com/questions/29072628/how-can-i-override-spring-boot-application-properties-programmatically">More info for above alternative</a>
     * @see <a href="https://stackoverflow.com/questions/65890334/configure-spring-boots-with-custom-sslcontext-programmatically-for-mtls/65898905#65898905">Alternative to set properties in Java code is finicky</a>
     * @see <a href="https://stackoverflow.com/questions/47580247/optional-environment-variables-in-spring-app/47581132#47581132">Attempt: Defaulting properties with Java code within .properties</a>
     * @see <a href="https://stackoverflow.com/questions/64426386/spring-boot-controller-to-handle-all-requests-for-preprocessing-before-forwardin">Possible alternative: Filter requests before handling them in main server code</a>
     * @see <a href="https://mvysny.github.io/spring-boot-enable-http-https">Attempt: Tring the same as the correct answer, but with v1 logic</a>
     * @see <a href="https://stackoverflow.com/questions/19613562/how-can-i-specify-my-keystore-file-with-spring-boot-and-tomcat">Attempt: Trying to dynamically toggle .properties `server.ssl.key-store` based on existence</a>
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(SERVER_HTTP_PORT);

        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(connector);

        return tomcat;
    }


    // Note: We can't use `@Autowired CacheManager cacheManager` because the cache names array needs to be final,
    // and the only way to do that when mixing with the CacheManager is via manual `@Bean` method.
    // See:
    //  - https://www.javadevjournal.com/spring-boot/3-ways-to-configure-multiple-cache-managers-in-spring-boot/
    @Bean
    public CacheManager cacheManager(@Value("${org.animeatsume.cache.cache-names}") String[] cacheNames) {
        log.info("Caching activated for cache names: {}", (Object[]) cacheNames);

        return new ConcurrentMapCacheManager(cacheNames);
    }

    public CacheManager cacheManager() {
        return cacheManager(null);
    }

    // Cannot use runtime vars like those populated by `@Value()` inside `@CacheEvict` annotation
    // nor `@Scheduled` annotations b/c the string reference inside via `CacheManager` constructor
    // call will technically be null.
    // We also can't use the key from .properties to get the value like we can in `@Cacheable`.
    // Thus, manually clear the cache via `CacheManager`.
    //@CacheEvict(allEntries = true, value = "${org.animeatsume.cache.anime-title-search}")
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


    // Reading CLI args could also be done via `CommandLineRunner` or `ApplicationRunner`,
    // both of which should be done in the `ApplicationDriver.main()` method.
    // See:
    //  - https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#features.spring-application.command-line-runner
    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        if (!logEnvVars) {
            return;
        }

        printAllProperties();
    }

    public Map<String, Object> getProperties() {
        return getProperties(null);
    }
    public Map<String, Object> getProperties(@Nullable ConfigurableEnvironment env) {
        return getProperties(env, null);
    }
    public Map<String, Object> getProperties(@Nullable ConfigurableEnvironment env, @Nullable List<String> sourcesNamesFilter) {
        if (env == null) {
            env = this.env;
        }

        return getProperties(env.getPropertySources().stream().toList(), sourcesNamesFilter);
    }
    public Map<String, Object> getProperties(@Nullable List<PropertySource<?>> propertySources, @Nullable List<String> sourcesNamesFilter) {
        if (sourcesNamesFilter == null) {
            sourcesNamesFilter = new ArrayList<>();
        }

        Map<String, Object> propertiesMap = new HashMap<>();
        Set<String> propertySourcesNames = new HashSet<>(sourcesNamesFilter);

        for (PropertySource<?> propertySource : propertySources) {
            if (sourcesNamesFilter.size() > 0 && !propertySourcesNames.contains(propertySource.getName())) {
                continue;
            }

            Map<String, Object> propertySourceMap = new HashMap<>();

            if (propertySource.getSource() instanceof Map) {
                propertySourceMap.putAll((Map<String, Object>) propertySource.getSource());
            }

            propertySourceMap
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> {
                    String propKey = entry.getKey();
                    Object propVal = entry.getValue();
                    Object prevPropVal = propertiesMap.get(propKey);

                    if (propVal instanceof Object[]) {
                        propVal = new ArrayList<>(Arrays.asList(propVal));
                    }

                    if (prevPropVal != null) {
                        if (!(prevPropVal instanceof List)) {
                            prevPropVal = new ArrayList<>(Arrays.asList(prevPropVal));
                        }

                        if (propVal instanceof List) {
                            ((List) prevPropVal).addAll((List) propVal);
                        } else {
                            ((List) prevPropVal).add(propVal);
                        }

                        propVal = prevPropVal;
                    }

                    propertiesMap.put(propKey, propVal);
                });
        }

        return propertiesMap;
    }

    public void printProperties(ConfigurableEnvironment env, String label) {
        printProperties(env, label, null);
    }
    public void printProperties(ConfigurableEnvironment env, String label, @Nullable List<String> sourcesNamesFilter) {
        printProperties(getProperties(env, sourcesNamesFilter), label);
    }
    public void printProperties(Map<String, Object> propertiesMap, String label) {
        log.info("\n********** {} PROPERTIES **********", label);
        log.info("{}", propertiesMap);
        log.info("********** END {} PROPERTIES **********\n", label);
    }

    public void printAllProperties() {
        printProperties(env, "ALL");
    }

    public void printSystemProperties() {
        printProperties(new HashMap(System.getProperties()), "SYSTEM");
    }

    public void printAppProperties(ContextRefreshedEvent event) {
        printProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment(), "APP", Arrays.asList("application.properties"));
    }

    public void printSpringProperties(ContextRefreshedEvent event) {
        printProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment(), "APP (SPRING)");
    }
}
