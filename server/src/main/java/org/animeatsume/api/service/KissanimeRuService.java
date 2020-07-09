package org.animeatsume.api.service;

import io.webfolder.ui4j.api.browser.BrowserEngine;
import io.webfolder.ui4j.api.browser.BrowserFactory;
import io.webfolder.ui4j.api.browser.Page;
import io.webfolder.ui4j.api.browser.PageConfiguration;
import org.animeatsume.api.model.Anchor;
import org.animeatsume.api.model.CaptchaAttempt;
import org.animeatsume.api.model.TitleSearchRequest;
import org.animeatsume.api.model.KissanimeSearchResponse;
import org.animeatsume.api.model.KissanimeVideoHostResponse;
import org.animeatsume.api.utils.ObjectUtils;
import org.animeatsume.api.utils.AppProxy;
import org.animeatsume.api.utils.http.Requests;
import org.animeatsume.api.utils.regex.HtmlParser;
import org.animeatsume.api.utils.regex.RegexUtils;
import org.animeatsume.api.utils.ui4j.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
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
    private static final String ARE_YOU_HUMAN_IMG_PATH = "/Special/CapImg/";
    private static final String TITLE_SEARCH_URL = KISSANIME_ORIGIN + "/Search/SearchSuggestx";
    private static final String ARE_YOU_HUMAN_URL_PATH = "/Special/AreYouHuman2";
    private static final String ARE_YOU_HUMAN_FORM_ACTION_URL = KISSANIME_ORIGIN + ARE_YOU_HUMAN_URL_PATH;
    private static final String CLOUDFLARE_TITLE = "Just a moment";
    private static final String KISSANIME_TITLE = "KissAnime";
    private static final String BANNED_TITLE = "Access Denied";
    private static final String BANNED_URL = "https://kissanime.ru/ToYou/Banned/";
    private static final String MOCK_FIREFOX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0";
    private static final String COOKIE_AUTH_NAME = "cf_clearance";
    private static final String NOVEL_PLANET_QUERY_PARAM = "&s=nova";

    private final Boolean activateKissanime;
    private final int numAttemptsToBypassCloudflare;

    public KissanimeRuService(
        @Value("${org.animeatsume.activate-kissanime}") Boolean activateKissanime,
        @Value("${org.animeatsume.num-attempts-to-bypass-cloudflare}") Integer numAttemptsToBypassCloudflare
    ) {
        this.activateKissanime = activateKissanime;
        this.numAttemptsToBypassCloudflare = numAttemptsToBypassCloudflare;
        setup();
    }

    public void setup() {
        // Load Kissanime on app startup to avoid having to wait for
        // Cloudflare's DDoS delay
        CookieHandler.setDefault(new CookieManager());

        if (activateKissanime) {
            bypassCloudflareDdosScreen(false);
        }
    }

    @Async
    CompletableFuture<Boolean> bypassCloudflareDdosScreen() {
        return bypassCloudflareDdosScreen(true);
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
    CompletableFuture<Boolean> bypassCloudflareDdosScreen(boolean throwErrorOnFailure) {
        PageConfiguration pageConfiguration = new PageConfiguration();
        pageConfiguration.setUserAgent(MOCK_FIREFOX_USER_AGENT);

        Page kissanimePage = browser.navigate(KISSANIME_ORIGIN, pageConfiguration);

        for (int attempt = 0; attempt < numAttemptsToBypassCloudflare; attempt++) {
            String pageTitle = PageUtils.getTitle(kissanimePage);

            if (!pageTitle.contains(CLOUDFLARE_TITLE) && pageTitle.contains(KISSANIME_TITLE)) {
                log.info("Cloudflare has been bypassed, Kissanime is now accessible");
                kissanimePage.close();
                return CompletableFuture.completedFuture(true);
            }

            if (
                pageTitle.toLowerCase().contains(BANNED_TITLE.toLowerCase())
                || PageUtils.getUrl(kissanimePage).equalsIgnoreCase(BANNED_URL)
            ) {
                log.info("Redirected to banned page with title ({}) and URL ({}). Attempting circumventing by setting new proxy and re-navigating to {}",
                    pageTitle,
                    PageUtils.getUrl(kissanimePage),
                    KISSANIME_ORIGIN
                );
                kissanimePage.close();
                clearAllCookies();
                AppProxy.setHttpProxyToNewResidentialProxy();
                kissanimePage = browser.navigate(KISSANIME_ORIGIN, pageConfiguration);
            }

            log.info("Cloudflare was not bypassed (title = \"{}\"), trying attempt {}/{}", pageTitle, attempt+1, numAttemptsToBypassCloudflare);

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

        if (throwErrorOnFailure) {
            throw new RuntimeException("Cannot bypass Cloudflare or access Kissanime");
        }

        return CompletableFuture.completedFuture(false);
    }

    private void clearAllCookies() {
        CookieStore cookieStore = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
        cookieStore.removeAll();

        log.info("All cookies cleared.");
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

    void waitForCloudflareToAllowAccessToKissanime() {
        waitForCloudflareToAllowAccessToKissanime(false);
    }

    /*
     * User-Agent header and cf_clearance cookie matter.
     * Origin, etc. doesn't
     */
    void waitForCloudflareToAllowAccessToKissanime(boolean forceRefresh) {
        if (getAuthCookie().hasExpired() || forceRefresh) {
            log.info("Cookie has expired. Refreshing now...");

            try {
                bypassCloudflareDdosScreen().get();
            } catch (Exception e) {
                log.error("Refreshing Kissanime auth token has failed. Error: {}",
                    e.getMessage()
                );

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

    public KissanimeSearchResponse searchKissanimeTitles(TitleSearchRequest titleSearchRequest) {
        waitForCloudflareToAllowAccessToKissanime();
        String requestSearchTitle = titleSearchRequest.getTitle();

        log.info("Searching Kissanime for title ({}) ...", requestSearchTitle);

        HttpHeaders headers = getNecessaryRequestHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formDataBody = new LinkedMultiValueMap<>();
        formDataBody.add("type", "Anime");
        formDataBody.add("keyword", requestSearchTitle);

        ResponseEntity<String> searchResponse;

        try {
            searchResponse = new RestTemplate().exchange(
                TITLE_SEARCH_URL,
                HttpMethod.POST,
                new HttpEntity<>(formDataBody, headers),
                String.class
            );
        } catch (HttpStatusCodeException e) {
            log.error("Error searching for Kissanime title: status code ({}), response body ({}), error = {}",
                e.getStatusCode(),
                e.getResponseBodyAsString(),
                e.getMessage()
            );

            String errorHtmlBodyTitle = HtmlParser.getTitleTextFromHtml(e.getResponseBodyAsString());

            /*
             * Occasionally, Kissanime will throw a 503 (Service Temporarily Unavailable)
             * error even if the cookie is still valid.
             * In this case, force the Cloudflare bypass to re-run and then attempt to
             * search Kissanime again.
             */
            if (errorHtmlBodyTitle != null && errorHtmlBodyTitle.contains(CLOUDFLARE_TITLE)) {
                waitForCloudflareToAllowAccessToKissanime(true);
                return searchKissanimeTitles(titleSearchRequest);
            }

            throw e;
        }

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

        ResponseEntity<Void> response;

        try {
            response = noFollowRedirectsRequest.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(null, getNecessaryRequestHeaders()),
                Void.class
            );
        } catch (HttpStatusCodeException e) {
            log.error("Error determining if request is redirected: status code ({}), response body ({}), error = {}",
                e.getStatusCode(),
                e.getResponseBodyAsString(),
                e.getMessage()
            );

            String errorHtmlBodyTitle = HtmlParser.getTitleTextFromHtml(e.getResponseBodyAsString());

            if (errorHtmlBodyTitle != null && errorHtmlBodyTitle.contains(CLOUDFLARE_TITLE)) {
                waitForCloudflareToAllowAccessToKissanime(true);
                return requestIsRedirected(url);
            }

            throw e;
        }

        return response.getStatusCode() == HttpStatus.FOUND;
    }

    public KissanimeVideoHostResponse getBypassAreYouHumanPromptContent(String url) {
        log.info("Getting AreYouHuman images and prompt texts to bypass ({})", url);
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
        String imgVerificationIdAndSrcRegex = "(<img[^>]+?indexValue=['\"])(\\w+)(.*?src=['\"])([^'\"]+)";
        String spanBodyRegex = "(<span[^>]+>)([^<]+)";

        String formHtml = RegexUtils.getFirstMatchGroups(areYouHumanFormHtmlRegex, areYouHumanHtml, Pattern.CASE_INSENSITIVE).get(0);

        List<CaptchaAttempt> imgVerificationIdsAndSrcs = RegexUtils.getAllMatchesAndGroups(imgVerificationIdAndSrcRegex, formHtml, Pattern.CASE_INSENSITIVE)
            .stream()
            .map(imgMatchGroups -> {
                String imgSrcUrlRelativePath = imgMatchGroups.get(4);
                String imageId = imgSrcUrlRelativePath.substring(imgSrcUrlRelativePath.lastIndexOf("/") + 1);
                String formId = imgMatchGroups.get(2);
                return new CaptchaAttempt(formId, imageId, null, null);
            })
            .collect(Collectors.toList());
        List<String> promptsForImagesToSelect = RegexUtils.getAllMatchesAndGroups(spanBodyRegex, formHtml, Pattern.CASE_INSENSITIVE)
            .stream()
            .map(spanMatchGroups -> RegexUtils.strip(spanMatchGroups.get(2)))
            .collect(Collectors.toList());

        return new KissanimeVideoHostResponse(null, new KissanimeVideoHostResponse.CaptchaContent(promptsForImagesToSelect, imgVerificationIdsAndSrcs));
    }

    public boolean executeBypassAreYouHumanCheckWithDbEntries(String episodeUrl, List<String> captchaAnswers) {
        List<CaptchaAttempt> captchaAnswerRequests = captchaAnswers.stream()
            .map(formId -> new CaptchaAttempt(formId, null, null, null))
            .collect(Collectors.toList());

        return executeBypassAreYouHumanCheck(episodeUrl, captchaAnswerRequests);
    }

    public boolean executeBypassAreYouHumanCheck(String episodeUrl, List<CaptchaAttempt> captchaAnswers) {
        log.info("Attempting to bypass AreYouHuman check for URL ({}) and captcha answer ({})", episodeUrl, captchaAnswers);
        waitForCloudflareToAllowAccessToKissanime();
        String urlPathWithQueryParams = episodeUrl.replace(KISSANIME_ORIGIN, "");
        String captchaAnswerTextForKissanimeForm = captchaAnswers.stream()
            .map(CaptchaAttempt::getFormId)
            .collect(Collectors.joining(","));

        HttpHeaders headers = getNecessaryRequestHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML, MediaType.APPLICATION_XML));

        MultiValueMap<String, String> formDataBody = new LinkedMultiValueMap<>();
        formDataBody.add("reUrl", urlPathWithQueryParams);
        formDataBody.add("answerCap", captchaAnswerTextForKissanimeForm);

        RestTemplate captchaSolverRequest = Requests.getNoFollowRedirectsRestTemplate();

        ResponseEntity<Void> captchaSolverResponse = captchaSolverRequest.exchange(
            ARE_YOU_HUMAN_FORM_ACTION_URL,
            HttpMethod.POST,
            new HttpEntity<>(formDataBody, headers),
            Void.class
        );

        return captchaSolverResponse.getStatusCode() == HttpStatus.FOUND && !captchaSolverResponse.getHeaders().getLocation().toString().contains(ARE_YOU_HUMAN_URL_PATH);
    }

    public ResponseEntity<Resource> getKissanimeCaptchaImage(String imageId) {
        String areYouHumanImgUrlPrefix = KISSANIME_ORIGIN + ARE_YOU_HUMAN_IMG_PATH;
        String url = areYouHumanImgUrlPrefix + imageId;

        return new RestTemplate().exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(null, getNecessaryRequestHeaders()),
            Resource.class
        );
    }

    @Async
    public CompletableFuture<String> setCaptchaImageHash(CaptchaAttempt captchaAttempt) {
        String imageId = captchaAttempt.getImageId();
        ResponseEntity<Resource> kissanimeCaptchaImage = getKissanimeCaptchaImage(imageId);
        Resource imageResource = kissanimeCaptchaImage.getBody();
        String imageHash = ObjectUtils.hashResource(imageResource);

        captchaAttempt.setImageHash(imageHash);

        return CompletableFuture.completedFuture(imageHash);
    }

    public KissanimeVideoHostResponse getVideoHostUrlFromEpisodePage(String episodeUrl) {
        log.info("Getting video host iframe's src value for URL ({})", episodeUrl);
        waitForCloudflareToAllowAccessToKissanime();

        ResponseEntity<String> response;

        try {
            response = new RestTemplate().exchange(
                episodeUrl,
                HttpMethod.GET,
                new HttpEntity<>(null, getNecessaryRequestHeaders()),
                String.class
            );
        } catch (HttpStatusCodeException e) {
            log.error("Error getting video host: status code ({}), response body ({}), error = {}",
                e.getStatusCode(),
                e.getResponseBodyAsString(),
                e.getMessage()
            );

            String errorHtmlBodyTitle = HtmlParser.getTitleTextFromHtml(e.getResponseBodyAsString());

            if (errorHtmlBodyTitle != null && errorHtmlBodyTitle.contains(CLOUDFLARE_TITLE)) {
                waitForCloudflareToAllowAccessToKissanime(true);
                return getVideoHostUrlFromEpisodePage(episodeUrl);
            }

            throw e;
        }

        String episodePageHtml = response.getBody();

        String videoHostUrl = HtmlParser.getIframeSrc(episodePageHtml);

        return new KissanimeVideoHostResponse(videoHostUrl, null);
    }

    public String[] getShowAndEpisodeNamesFromEpisodeUrl(String url) {
        try {
            URI episodeUri = new URI(url);
            String pathMinusFirstSlash = episodeUri.getPath().substring(1);
            String[] split = pathMinusFirstSlash.split("/");
            String showName = split[1];
            String episodeName = split[2];

            return new String[] { showName, episodeName };
        } catch (Exception e) {
            log.error("Could not parse Kissanime URL ({}) for show and episode names. Error cause ({}), message = {}",
                url,
                e.getCause(),
                e.getMessage()
            );
        }

        return null;
    }
}
