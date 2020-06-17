package org.animeatsume.api.controller;

import io.webfolder.ui4j.api.browser.BrowserEngine;
import io.webfolder.ui4j.api.browser.BrowserFactory;
import io.webfolder.ui4j.api.browser.Page;
import io.webfolder.ui4j.api.browser.PageConfiguration;
import org.animeatsume.api.model.KissanimeSearchRequest;
import org.animeatsume.api.model.KissanimeSearchResponse;
import org.animeatsume.api.utils.ui4j.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class KissanimeRuController {
    private static final Logger log = LoggerFactory.getLogger(KissanimeRuController.class);

    // BrowserEngine is a singleton; improve performance by avoiding making `synchronized` calls in endpoint handling
    private static final BrowserEngine browser = BrowserFactory.getWebKit();

    private static final String KISSANIME_ORIGIN = "https://kissanime.ru";
    private static final String CLOUDFLARE_TITLE = "Just a moment";
    private static final String KISSANIME_TITLE = "KissAnime";
    private static final String TITLE_SEARCH_URL = "https://kissanime.ru/Search/SearchSuggestx";
    private static final int NUM_ATTEMPTS_TO_BYPASS_CLOUDFLARE = 10;
    private static final String mockFirefoxUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0";
    private static final String cookieAuthName = "cf_clearance";

    public KissanimeRuController() {
        setup();
    }

    @Async
    public void setup() {
        // Load Kissanime on app startup to avoid having to wait for
        // Cloudflare's DDoS delay
        CookieHandler.setDefault(new CookieManager());
        bypassCloudflareDdosScreen();
    }

    /**
     * Kissanime has a Cloudflare check when you first arrive on the page for the day
     * to ensure you're not a bot.
     * This check basically just waits 4 seconds and then sets a special identification
     * cookie.
     * Browsing to the page and waiting for it to load all this gives our browser the
     * cookie it needs.
     */
    @Async
    CompletableFuture<Boolean> bypassCloudflareDdosScreen() {
        PageConfiguration pageConfiguration = new PageConfiguration();
        pageConfiguration.setUserAgent(mockFirefoxUserAgent);

        Page kissanimePage = browser.navigate(KISSANIME_ORIGIN, pageConfiguration);

        for (int attempt = 0; attempt < NUM_ATTEMPTS_TO_BYPASS_CLOUDFLARE; attempt++) {
            String pageTitle = PageUtils.getTitle(kissanimePage);

            if (!pageTitle.contains(CLOUDFLARE_TITLE) && pageTitle.contains(KISSANIME_TITLE)) {
                log.info("Cloudflare has been bypassed, Kissanime is now accessible");
                kissanimePage.close();
                log.info("Auth cookie is = {}", getAuthCookie());
                return CompletableFuture.completedFuture(true);
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

    HttpCookie getAuthCookie() {
        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        List<HttpCookie> kissanimeCookies = cookieManager.getCookieStore().get(URI.create(KISSANIME_ORIGIN));

        HttpCookie authCookie = kissanimeCookies.stream()
            .filter(cookieKeyVal -> cookieKeyVal.getName().equals(cookieAuthName))
            .findFirst()
            .orElse(new HttpCookie(cookieAuthName, ""));

        log.info("Expired = {}, max age = {}", authCookie.hasExpired(), authCookie.getMaxAge());

        return authCookie;
    }

    void waitForCloudflareToAllowAccessToKissanime() {
        bypassCloudflareDdosScreen();
    }

    /*
     * User-Agent and cf_clearance cookie matter.
     * Origin, etc. doesn't
     */
    public KissanimeSearchResponse searchKissanimeTitles(KissanimeSearchRequest kissanimeSearchRequest) {
        String requestSearchTitle = kissanimeSearchRequest.getTitle();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", mockFirefoxUserAgent);

        MultiValueMap<String, String> formDataBody = new LinkedMultiValueMap<>();
        formDataBody.add("type", "Anime");
        formDataBody.add("keyword", requestSearchTitle);

        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(formDataBody, headers);

        ResponseEntity<String> searchResponse = new RestTemplate().exchange(
            TITLE_SEARCH_URL,
            HttpMethod.POST,
            httpRequest,
            String.class
        );

        String searchResults = searchResponse.getBody();

        log.info("Kissanime search response = {}", searchResults);

        if (searchResults != null && !searchResults.isEmpty()) {
            Matcher urlMatches = Pattern.compile("(?<=href=\")[^\"]+").matcher(searchResults);
            List<String> urls = new ArrayList<>();

            while (urlMatches.find()) {
                urls.add(urlMatches.group());
            }

            log.info("Resulting URLs = {}", urls);
            // TODO parse out URLs and titles
        }

        return new KissanimeSearchResponse();
    }
}
