package org.animeatsume.api.service;

import io.webfolder.ui4j.api.browser.BrowserEngine;
import io.webfolder.ui4j.api.browser.BrowserFactory;
import io.webfolder.ui4j.api.browser.Page;
import io.webfolder.ui4j.api.browser.PageConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.animeatsume.api.model.Anchor;
import org.animeatsume.api.model.KissanimeSearchRequest;
import org.animeatsume.api.model.KissanimeSearchResponse;
import org.animeatsume.api.utils.http.Requests;
import org.animeatsume.api.utils.regex.HtmlParser;
import org.animeatsume.api.utils.regex.RegexUtils;
import org.animeatsume.api.utils.ui4j.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class KissanimeRuService {
    private static final Logger log = LoggerFactory.getLogger(KissanimeRuService.class);

    // BrowserEngine is a singleton; improve performance by avoiding making `synchronized` calls in endpoint handling
    private static final BrowserEngine browser = BrowserFactory.getWebKit();

    private static final String KISSANIME_ORIGIN = "https://kissanime.ru";
    private static final String TITLE_SEARCH_URL = KISSANIME_ORIGIN + "/Search/SearchSuggestx";
    private static final String CLOUDFLARE_TITLE = "Just a moment";
    private static final String KISSANIME_TITLE = "KissAnime";
    private static final int NUM_ATTEMPTS_TO_BYPASS_CLOUDFLARE = 10;
    private static final String MOCK_FIREFOX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0";
    private static final String COOKIE_AUTH_NAME = "cf_clearance";
    private static final String NOVEL_PLANET_QUERY_PARAM = "&s=nova";

    public KissanimeRuService() {
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
    @Async
    CompletableFuture<Boolean> bypassCloudflareDdosScreen() {
        PageConfiguration pageConfiguration = new PageConfiguration();
        pageConfiguration.setUserAgent(MOCK_FIREFOX_USER_AGENT);

        Page kissanimePage = browser.navigate(KISSANIME_ORIGIN, pageConfiguration);

        for (int attempt = 0; attempt < NUM_ATTEMPTS_TO_BYPASS_CLOUDFLARE; attempt++) {
            String pageTitle = PageUtils.getTitle(kissanimePage);

            if (!pageTitle.contains(CLOUDFLARE_TITLE) && pageTitle.contains(KISSANIME_TITLE)) {
                log.info("Cloudflare has been bypassed, Kissanime is now accessible");
                kissanimePage.close();
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
            log.info("Cookie has expired. Refreshing now...");

            try {
                bypassCloudflareDdosScreen().get();
            } catch (Exception e) {
                log.error("Refreshing Kissanime auth token has failed. Error:");
                e.printStackTrace();

                throw new RuntimeException("Cannot bypass Cloudflare or access Kissanime");
            }
        }
    }

    private HttpHeaders getNecessaryRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.set("User-Agent", MOCK_FIREFOX_USER_AGENT);
        headers.set("Cookie", getAuthCookie().toString());
        headers.add("Origin", KISSANIME_ORIGIN);

        return headers;
    }

    public KissanimeSearchResponse searchKissanimeTitles(KissanimeSearchRequest kissanimeSearchRequest) {
        waitForCloudflareToAllowAccessToKissanime();
        String requestSearchTitle = kissanimeSearchRequest.getTitle();

        log.info("Searching Kissanime for title ({}) ...", requestSearchTitle);

        HttpHeaders headers = getNecessaryRequestHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formDataBody = new LinkedMultiValueMap<>();
        formDataBody.add("type", "Anime");
        formDataBody.add("keyword", requestSearchTitle);

        ResponseEntity<String> searchResponse = new RestTemplate().exchange(
            TITLE_SEARCH_URL,
            HttpMethod.POST,
            new HttpEntity<>(formDataBody, headers),
            String.class
        );

        String searchResults = searchResponse.getBody();

        if (searchResults != null && !searchResults.isEmpty()) {
            String anchorResultsWithoutSpan = searchResults.replaceAll("</?span>", "");
            List<String> anchorResultsList = Arrays.asList(anchorResultsWithoutSpan.split("><"));

            List<KissanimeSearchResponse.SearchResults> searchResponses = anchorResultsList.stream()
                .map(anchorString -> {
                    String url = HtmlParser.getUrlFromAnchor(anchorString);
                    String title = HtmlParser.getTextFromAnchor(anchorString);

                    return new KissanimeSearchResponse.SearchResults(url, title);
                })
                .collect(Collectors.toList());

            return new KissanimeSearchResponse(searchResponses);
        }

        return new KissanimeSearchResponse();
    }

    @Async
    public CompletableFuture<List<Anchor>> searchKissanimeEpisodes(String showUrl) {
        waitForCloudflareToAllowAccessToKissanime();
        log.info("Searching Kissanime for episode list at ({}) ...", showUrl);

        String showHtml = new RestTemplate().exchange(
            showUrl,
            HttpMethod.GET,
            new HttpEntity<>(null, getNecessaryRequestHeaders()),
            String.class
        ).getBody();

        String episodeAnchorRegex = "<a.*?href=\"/Anime[^\"]+\\?id[\\s\\S]+?</a>";

        List<List<String>> matchResults = RegexUtils.getAllMatchesAndGroups(episodeAnchorRegex, showHtml);

        List<Anchor> episodeLinks = matchResults.stream()
            .map(matchGroups -> {
                String anchorString = matchGroups.get(0); // get first match group, containing the whole anchor element
                String url = HtmlParser.getUrlFromAnchor(anchorString);
                String title = HtmlParser.getTextFromAnchor(anchorString);

                // Remove leading spaces/tabs/etc.
                title = RegexUtils.strip(title);

                // Add kissanime origin since URLs are relative.
                // Also, make URL choose NovelPlanet by default
                // since the NovelPlanet controller/scraper logic is already in place
                url = KISSANIME_ORIGIN + url + NOVEL_PLANET_QUERY_PARAM;

                return new Anchor(url, title);
            })
            .collect(Collectors.toList());

        return CompletableFuture.completedFuture(episodeLinks);
    }

    public boolean requestIsRedirected(String url) {
        waitForCloudflareToAllowAccessToKissanime();
        RestTemplate noFollowRedirectsRequest = Requests.getNoFollowRedirectsRestTemplate();

        ResponseEntity<Void> response = noFollowRedirectsRequest.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(null, getNecessaryRequestHeaders()),
            Void.class
        );

        return response.getStatusCode() == HttpStatus.FOUND;
    }

    @Data
    @AllArgsConstructor
    public static class BypassAreYouHumanCheckRequestFields {
        private String formActionUrl;
        private HttpHeaders headers;
        private MultiValueMap<String, String> formDataBody;
    }

    public List<BypassAreYouHumanCheckRequestFields> getAllBypassAreYouHumanConfigurations(String url) {
        waitForCloudflareToAllowAccessToKissanime();
        String areYouHumanHtml = new RestTemplate().exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(null, getNecessaryRequestHeaders()),
            String.class
        ).getBody();

        // Form action, images, and image-selection prompt texts are all nested inside the <form> element
        // so regex strings are relative to within the <form>, not the entire <html> string.
        String areYouHumanFormHtmlRegex = "(<form(?=[^>]+AreYouHuman)[\\s\\S]+?</form>)";
        String formActionUrlPathRegex = "(?<=action=[\"'])([^\"']+)";
        String imgVerificationIdAndSrcRegex = "(<img[^>]+?indexValue=['\"])(\\w+)(.*?src=['\"])([^'\"]+)";
        String spanBodyRegex = "(<span[^>]+>)([^<]+)";

        String formHtml = RegexUtils.getFirstMatchGroups(areYouHumanFormHtmlRegex, areYouHumanHtml, Pattern.CASE_INSENSITIVE).get(0);

        List<List<String>> imgVerificationIdsAndSrcs = RegexUtils.getAllMatchesAndGroups(imgVerificationIdAndSrcRegex, formHtml, Pattern.CASE_INSENSITIVE)
            .stream()
            .map(imgMatchGroups -> Arrays.asList(imgMatchGroups.get(2), imgMatchGroups.get(4)))
            .collect(Collectors.toList());
        String formActionUrlPath = RegexUtils.getFirstMatchGroups(
            formActionUrlPathRegex,
            formHtml,
            Pattern.CASE_INSENSITIVE
        ).get(0);
        List<String> promptsForImagesToSelect = RegexUtils.getAllMatchesAndGroups(spanBodyRegex, formHtml, Pattern.CASE_INSENSITIVE)
            .stream()
            .map(spanMatchGroups -> RegexUtils.strip(spanMatchGroups.get(2)))
            .collect(Collectors.toList());

        String formActionUrl = KISSANIME_ORIGIN + formActionUrlPath;
        String urlPathWithQueryParams = url.replace(KISSANIME_ORIGIN, "");

        HttpHeaders headers = getNecessaryRequestHeaders();
        headers.add("Referer", formActionUrl + "?reUrl=" + urlPathWithQueryParams);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML, MediaType.APPLICATION_XML));

        log.info("imgVerificationIdsAndSrcs ({}), urlPathWithQuery ({}), formActionUrl ({}), referer ({})", imgVerificationIdsAndSrcs, urlPathWithQueryParams, formActionUrl, formActionUrl + "?reUrl=" + urlPathWithQueryParams);

        List<BypassAreYouHumanCheckRequestFields> bypassConfigs = new ArrayList<>();

        for (int i = 0; i < imgVerificationIdsAndSrcs.size(); i++) {
            for (int j = i+1; j < imgVerificationIdsAndSrcs.size(); j++) {
                String imgVerificationId1 = imgVerificationIdsAndSrcs.get(i).get(0);
                String imgVerificationId2 = imgVerificationIdsAndSrcs.get(j).get(0);

                MultiValueMap<String, String> formDataBody = new LinkedMultiValueMap<>();
                formDataBody.add("reUrl", urlPathWithQueryParams);
                formDataBody.add("answerCap", String.format("%s,%s,", imgVerificationId1, imgVerificationId2));

                bypassConfigs.add(new BypassAreYouHumanCheckRequestFields(formActionUrl, headers, formDataBody));
            }
        }

        return bypassConfigs;
    }

    @Async
    public CompletableFuture<String> executeBypassAreYouHumanCheck(BypassAreYouHumanCheckRequestFields configs) {
        waitForCloudflareToAllowAccessToKissanime();
        RestTemplate noFollowRedirectsRequest = Requests.getNoFollowRedirectsRestTemplate();

        ResponseEntity<String> searchResponse = noFollowRedirectsRequest.exchange(
            configs.getFormActionUrl(),
            HttpMethod.POST,
            new HttpEntity<>(configs.getFormDataBody(), configs.getHeaders()),
            String.class
        );

        if (searchResponse.getStatusCode() == HttpStatus.FOUND) {
            log.info("Redirect location = {}", searchResponse.getHeaders().getFirst("Location"));

            String successHtml = new RestTemplate().exchange(
                configs.getFormActionUrl(),
                HttpMethod.POST,
                new HttpEntity<>(configs.getFormDataBody(), configs.getHeaders()),
                String.class
            ).getBody();

            return CompletableFuture.completedFuture(successHtml);
        }

        return CompletableFuture.completedFuture(null);
    }
}
