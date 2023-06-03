package org.animeatsume.api.service;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.TitlesAndEpisodes.EpisodesForTitle;
import org.animeatsume.api.model.VideoSearchResult;
import org.animeatsume.api.model.nineanime.NineAnimeSearch;
import org.animeatsume.api.utils.ObjectUtils;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.regex.RegexUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Service
public class NineAnimeService {
    private static final String ORIGIN = "https://123anime.info";
    private static final String SEARCH_URL = ORIGIN + "/ajax/film/search?sort=year:desc&keyword=";
    private static final String SHOW_RESULTS_SELECTOR = "a.name";
    private static final String SHOW_NAVIGATE_SELECTOR = "a.btn-play";
    private static final String EPISODES_SELECTOR = "#episodes-content a";
    private static final String DOWNLOAD_ANCHOR_SELECTOR = "a.pc-download";  // The "Download" link that redirects to a different page
    private static final String DOWNLOAD_BUTTON_SELECTOR = ".dowload a";  // The different download links of varying video resolutions. Yes, they made a typo
    private static final String EPISODE_VIDEO_SOURCE_SELECTOR = "video source";

    @Value("${org.animeatsume.mock-firefox-user-agent}")
    private String mockFirefoxUserAgent;

    private static HttpHeaders getHeaders(String[][] headersToAdd) {
        HttpHeaders headers = new HttpHeaders();

        Arrays.asList(headersToAdd).forEach(header -> {
            headers.add(header[0], header[1]);
        });

        return headers;
    }

    private static HttpHeaders getSearchHeaders() {
        return getSearchHeaders(false);
    }

    private static HttpHeaders getSearchHeaders(boolean withCookie) {
        if (withCookie) {
            return getHeaders(new String[][] {
                { HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE },
                { HttpHeaders.COOKIE, getCookie() }
            });
        }

        return getHeaders(new String[][] {{ HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE }});
    }

    private static HttpHeaders getSearchHeadersJson() {
        return getHeaders(new String[][] {{ HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE }});
    }

    private static String getSearchUrl(String searchQuery) {
        return SEARCH_URL + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
    }

    private static String getEpisodeSearchUrl(String showUrl) {
        // e.g. `https://website.com/my-show` -> `my-show`
        String showSlug = showUrl.replaceAll("[^/]+/", "");

        return ORIGIN + "/ajax/film/sv?id=" + URLEncoder.encode(showSlug, StandardCharsets.UTF_8);
    }

    private static String getUrlWithOrigin(String... suffixes) {
        List<String> urlPaths = new ArrayList<>(Arrays.asList(suffixes));
        urlPaths.add(0, ORIGIN);
        String joinedUrl = String.join("/", urlPaths)
            .replaceAll("[\\\\/]\"(?=(\\\\|/|$))", "")
            .replaceAll("(?<!:)//", "/");

        return joinedUrl;
    }

    private static String getCookie() {
        List<String> setCookieHeader = CorsProxy.doCorsRequest(
            HttpMethod.GET,
            ORIGIN + "/state?ts=001&_=744",
            ORIGIN,
            null,
            getSearchHeadersJson()
        ).getHeaders().get(HttpHeaders.SET_COOKIE);

        if (setCookieHeader != null && setCookieHeader.size() > 0) {
            return setCookieHeader.get(0);
        }

        return "";
    }

    public TitlesAndEpisodes searchShows(String title) {
        String titleSearch = getSearchUrl(title);

        log.info("Searching <{}> for title ({}) ...", ORIGIN, titleSearch);

        ResponseEntity<?> showSearchResponse = CorsProxy.doCorsRequest(
            HttpMethod.GET,
            titleSearch,
            ORIGIN,
            null,
            getSearchHeadersJson()
        );

        Map<String, String> searchResponseJson = (Map<String, String>) showSearchResponse.getBody();
        String searchResponseHtml = searchResponseJson.get("html");

        if (searchResponseHtml != null) {
            Document showResultsDocument = Jsoup.parse(searchResponseHtml);
            Elements showAnchors = showResultsDocument.select(SHOW_RESULTS_SELECTOR);
            List<EpisodesForTitle> showResults = showAnchors
                .stream()
                .map(element -> new EpisodesForTitle(
                    getUrlWithOrigin(element.attr("href")),
                    element.text()
                ))
                .collect(Collectors.toList());

            log.info("Obtained {} show(s) for ({})", showResults.size(), title);

            return new TitlesAndEpisodes(showResults);
        }

        return null;
    }

    @Async
    public CompletableFuture<EpisodesForTitle> searchEpisodes(EpisodesForTitle episodesForTitle) {
        log.info("Searching <{}> for episode list at ({}) ...", ORIGIN, episodesForTitle.getUrl());

        String episodeSlug = getEpisodeSearchUrl(episodesForTitle.getUrl());
        String showSplashPage = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(episodeSlug),
            URI.create(ORIGIN),
            null,
            getSearchHeaders(true)
        ).getBody();
        String todo = "https://123anime.info/ajax/episode/info?epr=boruto-naruto-next-generations/001/5";
        Document showSearchHtml = Jsoup.parse(NineAnimeSearch.fromString(showSplashPage).getHtml());
        Elements servers = showSearchHtml.select(".tip.tab");
        String server = showSearchHtml.select(".server.mass").attr("data-id");
        Elements episodeAnchorElems = showSearchHtml.select(".episodes.range li a");
        log.info("episodeAnchorElems size ({})", episodeAnchorElems.size());

        if (episodeAnchorElems.size() < 1) {
            return null;
        };

        List<VideoSearchResult> episodeResults = episodeAnchorElems
            .stream()
            .map(element -> new VideoSearchResult(
                getUrlWithOrigin(element.attr("href")),
                element.text()
            ))
            .collect(Collectors.toList());

        if (episodeResults == null || episodeResults.size() == 0) {
            return null;
        }

        String showWatchUrl = getUrlWithOrigin(episodeResults.get(0).getUrl());

        log.info("Attempting to request: {}", showWatchUrl);

        String showHtml = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(showWatchUrl),
            URI.create(ORIGIN),
            null,
            getSearchHeaders(true)
        ).getBody();

        if (showHtml == null || showHtml.length() == 0) {
            return null;
        }

        List<VideoSearchResult> episodeAnchors = Jsoup.parse(showHtml)
            .select(EPISODES_SELECTOR)
            .stream()
            .map(element -> new VideoSearchResult(
                String.format("%s/%s", ORIGIN, element.attr("href")),
                element.attr("title")
            ))
            .collect(Collectors.toList());

        // TODO - I think this is unnecessary code leftover from merge conflict
        // episodesForTitle.setEpisodes(getDirectVideoUrls(episodeAnchors));

        log.info("Obtained {} episodes for ({})",
            episodesForTitle.getEpisodes().size(),
            episodesForTitle.getTitle()
        );

        getDirectVideoUrls(episodeAnchors);

        log.info("Determined URLs for {} episodes for ({})",
            episodesForTitle.getEpisodes().size(),
            episodesForTitle.getTitle()
        );

        return CompletableFuture.completedFuture(episodesForTitle);
    }

    public List<VideoSearchResult> getDirectVideoUrls(List<VideoSearchResult> episodeAnchors) {
        List<CompletableFuture<VideoSearchResult>> allEpisodesAndDownloadUrls = episodeAnchors
            .stream()
            .map(episodeAnchor -> {
                log.info("Getting 9anime MP4 video from ({})", episodeAnchor.getUrl());

                String episodeDownloadHtml = (String) CorsProxy.doCorsRequest(
                    HttpMethod.GET,
                    URI.create(episodeAnchor.getUrl()),
                    URI.create(ORIGIN),
                    null,
                    getSearchHeaders()
                ).getBody();

                String downloadPageUrl = Jsoup.parse(episodeDownloadHtml)
                    .select(DOWNLOAD_ANCHOR_SELECTOR)
                    .stream()
                    .map(element -> element.attr("href"))
                    .collect(Collectors.toList())
                    .get(0);

                String downloadPageHtml = (String) CorsProxy.doCorsRequest(
                    HttpMethod.GET,
                    URI.create(downloadPageUrl),
                    URI.create(ORIGIN),
                    null,
                    getSearchHeaders()
                ).getBody();

                VideoSearchResult highestQualityVideo = Jsoup.parse(downloadPageHtml)
                    .select(DOWNLOAD_BUTTON_SELECTOR)
                    .stream()
                    .map(element -> {
                        VideoSearchResult downloadLink = new VideoSearchResult(
                            element.attr("href"),
                            element.attr("title")
                        );

                        return downloadLink;
                    })
                    .sorted((VideoSearchResult a, VideoSearchResult b) -> {
                        String aResolution = RegexUtils.getAllMatchesAndGroups("\\d+(?=p)", a.getTitle()).get(0).get(0);
                        String bResolution = RegexUtils.getAllMatchesAndGroups("\\d+(?=p)", b.getTitle()).get(0).get(0);

                        if (aResolution == null || aResolution.length() == 0) {
                            return 1;
                        }

                        if (bResolution == null || bResolution.length() == 0) {
                            return -1;
                        }

                        return Integer.valueOf(bResolution).compareTo(Integer.parseInt(aResolution));
                    })
                    .collect(Collectors.toList())
                    .get(0);

                episodeAnchor.setUrl(highestQualityVideo.getUrl());

                return CompletableFuture.completedFuture(highestQualityVideo);
            })
            .collect(Collectors.toList());

        return ObjectUtils.getAllCompletableFutureResults(allEpisodesAndDownloadUrls);
    }

    public VideoSearchResult getVideosForShow(String url) {
        String episodeHtml = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(url),
            URI.create(ORIGIN),
            null,
            getSearchHeaders()
        ).getBody();

        if (episodeHtml != null) {
            Element videoSource = Jsoup.parse(episodeHtml)
                .select(EPISODE_VIDEO_SOURCE_SELECTOR)
                .first();
            String srcUrl = videoSource.attr("src");

            String secretVideoInIndexHtmlRegex = "(document.write.*href=\\\\\")(" + getDirectSourceVideoOriginsAsSearchRegex() + ".*?\\.mp4)";
            List<String> secretVideoInIndexHtmlMatches = RegexUtils.getFirstMatchGroups(
                secretVideoInIndexHtmlRegex,
                episodeHtml
            );

            if (!secretVideoInIndexHtmlMatches.isEmpty()) {
                int secretVideoInIndexHtmlCaptureGroupForSrc = 2;
                String secretVideoInIndexHtmlSrcUrl = secretVideoInIndexHtmlMatches.get(secretVideoInIndexHtmlCaptureGroupForSrc);

                log.info("9anime video src not in video.src property [{}], rather in index.html [{}]",
                    srcUrl,
                    secretVideoInIndexHtmlSrcUrl
                );

                srcUrl = secretVideoInIndexHtmlSrcUrl;
            }

            boolean isDirectSource = isVideoUrlDirectSource(srcUrl);
            List<String> videoQualityMatches = RegexUtils.getFirstMatchGroups("(\\d+p)(?=\\.mp4)", srcUrl);
            String videoQuality = "NA";

            if (!videoQualityMatches.isEmpty()) {
                videoQuality = videoQualityMatches.get(0);
            }

            // TODO see if prepending `storage.googleapis` to non-google-storage URLs works
            return new VideoSearchResult(srcUrl, videoQuality, isDirectSource);
        }

        return null;
    }

    public static boolean isVideoUrlDirectSource(String videoUrl) {
        return isVideoUrlDirectSource(videoUrl, false);
    }

    public static boolean isVideoUrlDirectSource(String videoUrl, boolean onlyFromSpecifiedSources) {
        if (onlyFromSpecifiedSources) {
            return videoUrl.matches(".*" + getDirectSourceVideoOriginsAsSearchRegex() + ".*");
        }

        try {
            // If we can get the headers from our origin without the CORS proxy,
            // then the client will be able to as well
            HttpHeaders httpHeaders = new RestTemplate().headForHeaders(videoUrl);

            return true;
        } catch (Exception e) {
            log.info("Video served from [{}] is not a direct source. Will have to proxy.", videoUrl);
        }

        return false;
    }

    private static String getDirectSourceVideoOriginsAsSearchRegex() {
        return "(" + String.join("|", /* DIRECT_SOURCE_VIDEO_ORIGINS */ "") + ")";
    }
}
