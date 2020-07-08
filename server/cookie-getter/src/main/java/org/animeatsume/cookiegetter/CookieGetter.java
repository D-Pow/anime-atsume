package org.animeatsume.cookiegetter;

import io.webfolder.ui4j.api.browser.BrowserEngine;
import io.webfolder.ui4j.api.browser.BrowserFactory;
import io.webfolder.ui4j.api.browser.Page;
import io.webfolder.ui4j.api.browser.PageConfiguration;
import org.springframework.stereotype.Component;

import java.net.*;
import java.net.HttpCookie;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class CookieGetter {
    // BrowserEngine is a singleton; improve performance by avoiding making `synchronized` calls in endpoint handling
    private static final BrowserEngine browser = BrowserFactory.getWebKit();

    private static final String KISSANIME_ORIGIN = "https://kissanime.ru";
    private static final String ARE_YOU_HUMAN_IMG_PATH = "/Special/CapImg/";
    private static final String TITLE_SEARCH_URL = KISSANIME_ORIGIN + "/Search/SearchSuggestx";
    private static final String ARE_YOU_HUMAN_URL_PATH = "/Special/AreYouHuman2";
    private static final String ARE_YOU_HUMAN_FORM_ACTION_URL = KISSANIME_ORIGIN + ARE_YOU_HUMAN_URL_PATH;
    private static final String CLOUDFLARE_TITLE = "Just a moment";
    private static final String KISSANIME_TITLE = "KissAnime";
    private static final int NUM_ATTEMPTS_TO_BYPASS_CLOUDFLARE = 5;
    private static final String MOCK_FIREFOX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0";
    private static final String COOKIE_AUTH_NAME = "cf_clearance";
    private static final String NOVEL_PLANET_QUERY_PARAM = "&s=nova";

    public CookieGetter() {
        setup();
    }

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
    CompletableFuture<Boolean> bypassCloudflareDdosScreen() {
        PageConfiguration pageConfiguration = new PageConfiguration();
        pageConfiguration.setUserAgent(MOCK_FIREFOX_USER_AGENT);

        Page kissanimePage = browser.navigate(KISSANIME_ORIGIN, pageConfiguration);

        for (int attempt = 0; attempt < NUM_ATTEMPTS_TO_BYPASS_CLOUDFLARE; attempt++) {
            String pageTitle = kissanimePage.getDocument().getTitle().orElse("");

            if (!pageTitle.contains(CLOUDFLARE_TITLE) && pageTitle.contains(KISSANIME_TITLE)) {
                System.out.println("Cloudflare has been bypassed, Kissanime is now accessible");
                kissanimePage.close();
                return CompletableFuture.completedFuture(true);
            }

            System.out.println("Cloudflare was not bypassed, trying attempt " + (attempt + 1) + "/" + (NUM_ATTEMPTS_TO_BYPASS_CLOUDFLARE));
            System.out.println("Page title is: " + pageTitle);

            try {
                // Cloudflare has 4000 ms countdown before redirecting.
                // Add an extra second to ensure it's actually redirected.
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println("Error while waiting for initial Kissanime page load in initialization:");
                e.printStackTrace();
            }
        }

//        log.error("Could not bypass Cloudflare. Stuck on URL ({}) with title ({}) and body text ({})",
//            PageUtils.getUrl(kissanimePage),
//            PageUtils.getTitle(kissanimePage),
//            PageUtils.getInnerText(kissanimePage)
//        );

        throw new RuntimeException("Cannot bypass Cloudflare or access Kissanime");
    }

    public HttpCookie getAuthCookie() {
        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        List<HttpCookie> kissanimeCookies = cookieManager.getCookieStore().get(URI.create(KISSANIME_ORIGIN));

        HttpCookie placeholderCookie = new HttpCookie(COOKIE_AUTH_NAME, "");
        placeholderCookie.setMaxAge(0);

        HttpCookie authCookie = kissanimeCookies.stream()
            .filter(cookieKeyVal -> cookieKeyVal.getName().equals(COOKIE_AUTH_NAME))
            .findFirst()
            .orElse(placeholderCookie);

        return authCookie;
    }

    /*
     * User-Agent header and cf_clearance cookie matter.
     * Origin, etc. doesn't
     */
    void waitForCloudflareToAllowAccessToKissanime() {
        if (getAuthCookie().hasExpired()) {
            System.out.println("Cookie has expired. Refreshing now...");

            try {
                bypassCloudflareDdosScreen().get();
            } catch (Exception e) {
                System.out.println("Refreshing Kissanime auth token has failed. Error:");
                e.printStackTrace();

                throw new RuntimeException("Cannot bypass Cloudflare or access Kissanime");
            }
        }
    }
}
