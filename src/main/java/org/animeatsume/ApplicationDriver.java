package org.animeatsume;

import org.animeatsume.api.model.NovelPlanetSourceResponse;
import org.animeatsume.api.utils.http.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SpringBootApplication is a combo of the following:
 *  Configuration - Java component configuration with beans using vanilla Spring framework, e.g. `@Bean` or `@Controller`.
 *  ComponentScan - Specifies the packages to scan for @Configuration to recognize; searches current package
 *      and all subpackages, so best to put this main driver file in module root classpath.
 *      This handles the bulk of the work required for @Autowired annotations to function properly.
 *  EnableAutoConfiguration - Allows SpringBoot to create many beans and configure the application automatically
 *      based on the resolved classpath; also allows for selective include/exclude/etc. fields.
 *      This handles the bulk of the work required for functionality required by specific classes (e.g. @Repository,
 *      JpaRepository interfaces, @DataSource, factories, etc.) so you don't have to manually create a beans.xml file
 *      for each of these specific classes.
 */
// Remove 'exclude' once database is added
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class ApplicationDriver {
    private static final Logger log = LoggerFactory.getLogger(ApplicationDriver.class);

    public static void main(String[] args) {
        SpringApplication.run(ApplicationDriver.class, args);
    }

    @Bean
    public CommandLineRunner run() throws Exception {
        return args -> {
            String novelPlanetOrigin = "https://www.novelplanet.me";
            String videoId = "3qv17n2d-2v";
            String novelPlanetWebsiteUrl = novelPlanetOrigin + "/v/" + videoId;
            String novelPlanetApiUrl = novelPlanetOrigin + "/api/source/" + videoId;

            RestTemplate websiteRequest = new RestTemplate();
            ResponseEntity<String> websiteHtml = websiteRequest.getForEntity(novelPlanetWebsiteUrl, String.class);
            String websiteCookie = websiteHtml.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

            HttpEntity<Void> request = Requests.getHttpEntityWithHeaders(null, new String[][] {
                { "Origin", novelPlanetOrigin },
                { "Referer", novelPlanetWebsiteUrl },
                { "Cookie", websiteCookie },
                { "sec-fetch-dest", "empty" },
                { "sec-fetch-mode", "cors" },
                { "sec-fetch-site", "same-origin" },
                { "dnt", "1" },
                { "Pragma", "no-cache" },
                { "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.122 Safari/537.36" }
            });

            ResponseEntity<NovelPlanetSourceResponse> response = new RestTemplate().exchange(
                novelPlanetApiUrl,
                HttpMethod.POST,
                request,
                NovelPlanetSourceResponse.class
            );

            NovelPlanetSourceResponse novelPlanetResponse = response.getBody();

            List<String> mp4Urls = novelPlanetResponse.getData().stream()
                .map(novelPlanetSource -> {
                    String redirectorUrl = novelPlanetSource.getFile();

                    HttpEntity<Void> mp4Request = Requests.getHttpEntityWithHeaders(null, new String[][] {
                        { "Cookie", websiteCookie },
                        { "Referer", novelPlanetWebsiteUrl }
                    });

                    // will give 302 (Found) with redirect. Don't follow it, instead get the redirect URL
                    // since that holds the URL to the MP4
                    RestTemplate redirectorRequest = Requests.getNoFollowRedirectsRestTemplate();

                    ResponseEntity<Void> redirectorResponse = redirectorRequest.exchange(
                        redirectorUrl,
                        HttpMethod.GET,
                        mp4Request,
                        Void.class
                    );

                    return redirectorResponse.getHeaders().getFirst("Location");
                })
                .collect(Collectors.toList());

            mp4Urls.forEach(log::info);
        };
    }
}
