package org.animeatsume.api.controller;

import io.webfolder.ui4j.api.browser.BrowserEngine;
import io.webfolder.ui4j.api.browser.BrowserFactory;
import io.webfolder.ui4j.api.browser.Page;
import io.webfolder.ui4j.api.browser.PageConfiguration;
import io.webfolder.ui4j.api.interceptor.Interceptor;
import io.webfolder.ui4j.api.interceptor.Request;
import io.webfolder.ui4j.api.interceptor.Response;
import javafx.application.Platform;
import org.animeatsume.api.model.KissanimeSearchRequest;
import org.animeatsume.api.utils.ui4j.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class KissanimeRuController {
    private static final Logger log = LoggerFactory.getLogger(KissanimeRuController.class);

    // BrowserEngine is a singleton; improve performance by avoiding making `synchronized` calls in endpoint handling
    private static final BrowserEngine browser = BrowserFactory.getWebKit();
    private static PageConfiguration pageConfiguration;
    private static Response browserNavigateResponse;

    private static final String KISSANIME_ORIGIN = "https://kissanime.ru";
    private static final String CLOUDFLARE_TITLE = "Just a moment";
    private static final String KISSANIME_TITLE = "KissAnime";
    private static final int NUM_ATTEMPTS_TO_BYPASS_CLOUDFLARE = 10;
    private static final String TITLE_SEARCH_URL = "https://kissanime.ru/Search/SearchSuggestx";


    static {
        // Load Kissanime on app startup to avoid having to wait for
        // Cloudflare's DDoS delay
        setupPageConfigurationWithResponseInterceptor();
        // TODO need to do Spring's async stuff
        Platform.runLater(KissanimeRuController::bypassCloudflareDdosScreen);
    }

    static void setupPageConfigurationWithResponseInterceptor() {
        pageConfiguration = new PageConfiguration(new Interceptor() {
            @Override
            public void beforeLoad(Request request) {}

            @Override
            public void afterLoad(Response response) {
                browserNavigateResponse = response;
            }
        });
    }

    /**
     * Kissanime has a Cloudflare check when you first arrive on the page for the day
     * to ensure you're not a bot.
     * This check basically just waits 4 seconds and then sets a special identification
     * cookie.
     * Browsing to the page and waiting for it to load all this gives our browser the
     * cookie it needs.
     */
    private static void bypassCloudflareDdosScreen() {
        Page kissanimePage = browser.navigate(KISSANIME_ORIGIN, pageConfiguration);

        for (int attempt = 0; attempt < NUM_ATTEMPTS_TO_BYPASS_CLOUDFLARE; attempt++) {
            String pageTitle = PageUtils.getTitle(kissanimePage);

            if (!pageTitle.contains(CLOUDFLARE_TITLE) && pageTitle.contains(KISSANIME_TITLE)) {
                log.info("Cloudflare has been bypassed, Kissanime is now accessible");
                return;
            }

            log.info("Cloudflare was not bypassed, trying attempt {}/10", attempt+1);

            try {
                // Cloudflare has 4000 ms countdown before redirecting.
                // Add an extra second to ensure it's actually redirected.
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("Error while waiting for initial Kissanime page load in initialization:");
                e.printStackTrace();
            }
        }

        log.error("Could not bypass Cloudflare. Stuck on URL ({}) with title ({}) and body text ({})",
            PageUtils.getUrl(kissanimePage),
            PageUtils.getTitle(kissanimePage),
            PageUtils.getInnerText(kissanimePage)
        );

        throw new RuntimeException("Cannot bypass Cloudflare or access Kissanime");
    }

    private static void waitForCloudflareToAllowAccessToKissanime() {
        bypassCloudflareDdosScreen();
    }

    /*
     * User-Agent and cf_clearance cookie matter.
     * Origin, etc. doesn't
     */
    public void searchKissanimeTitles(KissanimeSearchRequest kissanimeSearchRequest) {
        String title = kissanimeSearchRequest.getTitle();
        log.info("User agent: {}", pageConfiguration.getUserAgent());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Cookie", browserNavigateResponse.getCookie("cf_clearance").orElse(null).getValue());
        headers.set("User-Agent", pageConfiguration.getUserAgent());

        MultiValueMap<String, String> formDataBody = new LinkedMultiValueMap<>();
        formDataBody.add("type", "Anime");
        formDataBody.add("keyword", title);

        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(formDataBody, headers);

        ResponseEntity<String> searchResponse = new RestTemplate().exchange(
            TITLE_SEARCH_URL,
            HttpMethod.POST,
            httpRequest,
            String.class
        );

        log.info("Kissanime search response = {}", searchResponse.getBody());
//        Page kissanime = browser.navigate("https://kissanime.ru/Anime/Shigatsu-wa-Kimi-no-Uso");
    }
}
