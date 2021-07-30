package org.animeatsume.api.service;

import lombok.extern.slf4j.Slf4j;
import org.animeatsume.api.model.Anchor;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.TitlesAndEpisodes.EpisodesForTitle;
import org.animeatsume.api.model.VideoSearchResult;
import org.animeatsume.api.model.kimanime.TitleSearchRequest;
import org.animeatsume.api.model.kimanime.TitleSearchResults;
import org.animeatsume.api.utils.ObjectUtils;
import org.animeatsume.api.utils.http.Cookies;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.http.UriParser;
import org.animeatsume.api.utils.regex.RegexUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KimAnimeService {
    private static final String ORIGIN = "https://kimanime.com";  // UriParser.getOrigin(URI.create(ORIGIN))
    private static final String SEARCH_URL = ORIGIN + "/api/anime/get/list";

    private static final String XSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";

    private static final List<String> DIRECT_SOURCE_VIDEO_ORIGINS = Arrays.asList("https://storage.googleapis.com", "https://[^\\.]+.4animu.me");
    private static final String TITLE_ANCHOR_SELECTOR = "a.name";
    private static final String EPISODE_ANCHOR_SELECTOR = "ul.episodes a[title]";
    private static final String EPISODE_VIDEO_SOURCE_SELECTOR = "video source";

    @Value("${org.animeatsume.mock-firefox-user-agent}")
    private String mockFirefoxUserAgent;

    private String xsrfToken = "";

    private void resetXsrfToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.ALL));
        headers.set(HttpHeaders.USER_AGENT, mockFirefoxUserAgent);

//        ResponseEntity<String> homePage = (ResponseEntity<String>) CorsProxy.doCorsRequest(HttpMethod.GET, URI.create(ORIGIN), null, null, headers);
//        List<String> setCookieHeaders = homePage.getHeaders().get(HttpHeaders.SET_COOKIE);
        List<String> setCookieHeaders = Cookies.getCookiesFromWebsite(ORIGIN);

        String xsrfTokenHeader = ObjectUtils.findObjectInList(setCookieHeaders, str -> str.contains(XSRF_TOKEN_COOKIE_NAME));
        String xsrfToken = RegexUtils.getFirstMatchGroups(String.format("(?<=%s=)([^;]+)(?=;)", XSRF_TOKEN_COOKIE_NAME), xsrfTokenHeader).get(0);

        this.xsrfToken = xsrfToken;

        log.info("Got new KimAnime {}: {}", XSRF_TOKEN_COOKIE_NAME, xsrfToken);
    }

    private HttpHeaders getNecessaryRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.set(HttpHeaders.USER_AGENT, mockFirefoxUserAgent);
        headers.set(
            MessageFormat.format("X-{0}", XSRF_TOKEN_COOKIE_NAME).toLowerCase(), // x-xsrf-token=<token>
            this.xsrfToken
        );
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.ALL));

        return headers;
    }

    private <O> O attemptRequestWithXsrfToken(Supplier<O> requestAttemptFunc) {
        if (xsrfToken.isEmpty()) {
            resetXsrfToken();
        }

        try {
            // TODO probably need to get the token from the failed ResponseEntity, not the one from index.html
            return requestAttemptFunc.get();
        } catch (RestClientException e) {
            resetXsrfToken();

            return requestAttemptFunc.get();
        }
    }

//    (async () => {
//    const res = await fetch('https://kimanime.com/api/anime/get/list', {
//            method: 'POST',
//            headers: {
//            Accept: 'application/json',
//                'x-xsrf-token': 'eyJpdiI6ImxkQ3NHOWtDWnpzb1JiYWl0b2RNRkE9PSIsInZhbHVlIjoieXlOZE9qNmZ0Wm9yVmpObTZFSnRHY0UzNG5MZTZ0enNHZGovZnJhUTNSTEFYb1dla1p5Rjk1YmN4S0plTkw1TjE0U1kwZkZzSTF3UWtaOTYyUUZXSTdPRVpESFdSWlEvTFl6Uy9TOENxcnVSL3dUOWdsdFUxUjNDU0x1OGQyVEsiLCJtYWMiOiJiMjNiYTUzZTAwNTcwZjBlMTI0ZDM3NmMzMzM5YTBjMDIyYmQ4YWUzODc2MjQwNTNhN2I5ZmQ5OTliZmQ4ZWVkIn0=',
//        },
//        body: JSON.stringify({
//            language: 'Both',
//            letter: 'All',
//            sort: 'MostMatched',
//            status: 'All',
//            text: 'naruto',
//            type: 'OVA'
//        })
//    });
//    const json = await res.json();
//
//        console.log(json)
//    })()
//
//
//    let header = 'set-cookie'
//    let headerKey = 'XSRF-TOKEN'
    @Async
    public TitlesAndEpisodes searchTitle(String title) {
        TitleSearchRequest searchRequest = new TitleSearchRequest(title);

        HttpEntity<TitleSearchRequest> corsSearchRequest = CorsProxy.getCorsEntity(
            searchRequest,
            ORIGIN,
            ORIGIN,
//            MessageFormat.format("{0}={1}", XSRF_TOKEN_COOKIE_NAME, xsrfToken),
            null,
            getNecessaryRequestHeaders(),
            false
        );

        Object res = (Object) attemptRequestWithXsrfToken(() ->
            new RestTemplate().exchange(SEARCH_URL,
                HttpMethod.POST,
                corsSearchRequest,
                TitleSearchResults.class
            )
//            CorsProxy.doCorsRequest(
//                HttpMethod.POST,
//                URI.create(SEARCH_URL),
//                null,
//                searchRequest,
//                getNecessaryRequestHeaders()
//            ).getBody()
        );

        log.info("TITLE response: {}", res);

        TitleSearchResults searchResults = (TitleSearchResults) res;

        TitlesAndEpisodes titlesAndEpisodesResponse = new TitlesAndEpisodes();

        searchResults.getResult().getData()
            .forEach(showInfo -> {
                VideoSearchResult showResult = new VideoSearchResult(showInfo.getShowUrl(), showInfo.getTitle());

//                titlesAndEpisodesResponse.getResults().add(showResult);
                log.info("VideoSearchResult: {}", showResult);
            });
//            .collect(Collectors.toList());

        return titlesAndEpisodesResponse;
    }

    @Async
    public CompletableFuture<Void> searchEpisodes(EpisodesForTitle episodesForTitle) {
        return CompletableFuture.completedFuture(null);
    }
}
