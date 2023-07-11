package org.animeatsume.api.service;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.model.Anchor;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.TitlesAndEpisodes.EpisodesForTitle;
import org.animeatsume.api.model.VideoSearchResult;
import org.animeatsume.api.model.nineanime.NineAnimeEpisodeHostResponse;
import org.animeatsume.api.model.nineanime.NineAnimeSearchResponse;
import org.animeatsume.api.utils.ObjectUtils;
import org.animeatsume.api.utils.SeleniumService;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.http.UriParser;
import org.animeatsume.api.utils.regex.RegexUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String SHOW_INFO_URL = ORIGIN + "/ajax/film/sv?id=";
    private static final String EPISODES_INFO_URL = ORIGIN + "/ajax/episode/info?epr=";
    private static final String SHOW_RESULTS_SELECTOR = "a.name";
    private static final String SHOW_NAVIGATE_SELECTOR = "a.btn-play";
    private static final String EPISODES_SELECTOR = "#episodes-content a";
    private static final String DOWNLOAD_ANCHOR_SELECTOR = "a.pc-download";  // The "Download" link that redirects to a different page
    private static final String DOWNLOAD_BUTTON_SELECTOR = ".dowload a";  // The different download links of varying video resolutions. Yes, they made a typo
    private static final String EPISODE_VIDEO_SOURCE_SELECTOR = "video source";

    @Autowired
    private SeleniumService seleniumService; // = new SeleniumService();

//    private SeleniumService seleniumService; // = new SeleniumService();

//    public NineAnimeService() {
//        seleniumService = new SeleniumService();
//    }

    private static HttpHeaders getSearchHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        return headers;
    }

    private static String getSearchUrl(String searchQuery) {
        return SEARCH_URL + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
    }

    private static String getShowInfoUrl(String searchQuery) {
        return SHOW_INFO_URL + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
    }

    private static URI getEpisodePageUrl(String showId, String episodeId) {
        return getEpisodePageUrl(showId, episodeId, "0");
    }

    /**
     * e.g.
     * <pre>
     * input: "/anime/naruto-shippuden/episode/001", "5" ->
     *  showId = "naruto-shippuden"
     *  episodeId = "001"
     *  videoHostIndex = "0" (default)
     *
     * output: "/ajax/episode/info?epr=naruto-shippuden/001/5"
     * </pre>
     *
     * @param showId - Show name/ID.
     * @param episodeId - Episode number (e.g. "001").
     * @param videoHostIndex - Video host (e.g. "0").
     * @return URL of the actual video host (a third-party unassociated with {@code ORIGIN}).
     */
    private static URI getEpisodePageUrl(String showId, String episodeId, String videoHostIndex) {
        String episodeBaseUrl = EPISODES_INFO_URL + showId;
        String episodePageUrl = String.join("/", episodeBaseUrl, episodeId, videoHostIndex)
            .replaceAll("(?<!https:)/{2,}", "/");

        return URI.create(episodePageUrl);
    }

    private static String getUrlWithOrigin(String... suffixes) {
        List<String> urlPaths = new ArrayList<>(Arrays.asList(suffixes));

        urlPaths.add(0, ORIGIN);

        String joinedUrl = String.join("/", urlPaths)
            .replaceAll("(?<!https:)/{2,}", "/")
            .replaceAll("[\\\\/]\"(?=(\\\\|/|$))", "");

        return joinedUrl;
    }

    public TitlesAndEpisodes searchShows(String title) {
        String titleSearch = getSearchUrl(title);

        log.info("Searching <{}> for title ({}) ...", ORIGIN, titleSearch);

        HttpHeaders searchShowsHeaders = new HttpHeaders();
        searchShowsHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> searchResponseJson = (Map) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            titleSearch,
            ORIGIN,
            null,
            searchShowsHeaders
        ).getBody();

        String searchResponseHtml = searchResponseJson.getOrDefault("html", null);

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
        /*
         * e.g. "https://123anime.info/ajax/episode/info?epr=naruto-shippuden/001/5"
         *  episodesForTitle.getUrl() & showSearchUrl = https://123anime.info/anime/naruto-shippuden
         *  showInfoUrl = https://123anime.info/ajax/film/sv?id=naruto-shippuden
         *  showSearchUrl = /anime/naruto-shippuden/episode/001
         *  showId = naruto-shippuden
         *  episodeId = 001
         *  videoPlayerIndex = 5
         */
        URI showSearchUrl = URI.create(episodesForTitle.getUrl());
        String showId = UriParser.getPathSegment(showSearchUrl, -1);
        URI showInfoUrl = URI.create(getShowInfoUrl(showId));

        log.info("Searching <{}> for episode list at ({}) ...", episodesForTitle.getUrl(), showInfoUrl);

        String showSplashPageResponse = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            showInfoUrl,
            URI.create(ORIGIN),
            null,
            getSearchHeaders()
        ).getBody();

        NineAnimeSearchResponse showSplashPage = NineAnimeSearchResponse.fromString(showSplashPageResponse);
        Elements showEpisodesAnchors = Jsoup.parse(showSplashPage.getHtml()).select(".episodes.range li a");

        log.info("Num episodes found at <{}>: {}", showInfoUrl, showEpisodesAnchors.size());

        if (showEpisodesAnchors.size() < 1) {
            return null;
        }

        List<Anchor> episodeAnchors = showEpisodesAnchors.stream().map(anchor -> {
            // e.g. "001"
            String episodeId = anchor.text();
            // e.g. "/anime/naruto-shippuden/episode/001"
            String episodeUrl = getUrlWithOrigin(anchor.attr("href"));

            // e.g. "naruto-shippuden/001/5"
            Anchor episodeAnchor = new Anchor(episodeUrl, episodeId);

            return episodeAnchor;
        }).toList();

        List<VideoSearchResult> episodeHosts = episodeAnchors.stream().map(anchor -> {
            // Remove leading zeros via casting to int, e.g. "001" -> "1"
            String episodeTitle = String.valueOf(Integer.parseInt(anchor.getTitle()));
            String episodeInfoSearchUrl = getEpisodePageUrl(showId, anchor.getTitle()).toString();

            return new VideoSearchResult(episodeInfoSearchUrl, episodeTitle);
        }).toList();

        episodesForTitle.setEpisodes(episodeHosts);

        log.info("episodesForTitle: {}", episodesForTitle);

        return CompletableFuture.completedFuture(episodesForTitle);
    }

    public VideoSearchResult getVideosForShow(String url) {
        String episodeHostResponseJson = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(url),
            URI.create(ORIGIN),
            null,
            getSearchHeaders()
        ).getBody();

        NineAnimeEpisodeHostResponse episodeHost = NineAnimeEpisodeHostResponse.fromString(episodeHostResponseJson);
        String episodeHostUrl = episodeHost.getTarget();

        HttpHeaders getVideoUrlFromHostHeaders = getSearchHeaders();
        getVideoUrlFromHostHeaders.add(HttpHeaders.REFERER, UriParser.getOrigin(url));

        // `Location` header excludes "https:" but maintains "//website.com"
        String videoUrl = "https:" + CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(episodeHostUrl),
            URI.create(UriParser.getOrigin(episodeHostUrl)),
            null,
            getVideoUrlFromHostHeaders
        ).getHeaders().getLocation().toString();

        ResponseEntity<String> videoResponse = (ResponseEntity<String>) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(videoUrl),
            URI.create(UriParser.getOrigin(videoUrl)),
            null,
            getVideoUrlFromHostHeaders
        );

        if (videoResponse.getHeaders().getLocation() != null) {
            videoUrl = videoResponse.getHeaders().getLocation().toString();

            videoResponse = (ResponseEntity<String>) CorsProxy.doCorsRequest(
                HttpMethod.GET,
                URI.create(videoUrl),
                URI.create(UriParser.getOrigin(videoUrl)),
                null,
                getVideoUrlFromHostHeaders
            );
        }

        String episodeHostResponseHtml = videoResponse.getBody();

        List<String> hostUrls = Jsoup.parse(episodeHostResponseHtml).select("li.linkserver").stream()
            .map(li -> li.attr("data-video"))
            .filter(videoHostUrl -> !videoHostUrl.isBlank())
            .toList();
        String primaryHostUrl = hostUrls.get(0);

        io.webfolder.ui4j.api.dom.Element videoElem = this.seleniumService.clickOn(primaryHostUrl, "video");
        String videoElemSrc = videoElem.getAttribute("src");
        String videoUrlDirect = videoElemSrc != null && !videoElemSrc.isBlank()
            ? videoElemSrc
            : primaryHostUrl;

//        videoUrlDirect

        log.info("BEFORE WHILE: videoElem.getAttribute(\"src\"): {}", videoUrlDirect);

        while (
            videoElemSrc == null
            || videoElemSrc.isBlank()
        ) {
            videoElemSrc = videoElem.getAttribute("src");

            log.info("IN WHILE: videoElem.getAttribute(\"src\"): {}", videoElemSrc);

            try {
                videoUrlDirect = videoElemSrc;

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Thread.sleep() error: {}", e.getMessage());
            }
        }

        log.info("Obtained video host: <{}>  ->  <{}>  ->  <{}>  ->  <{}>  ->  <{}>",
            url,
            episodeHostUrl,
            videoUrl,
            primaryHostUrl,
            videoUrlDirect
        );

        return new VideoSearchResult(videoUrlDirect, UriParser.getQueryParams(url).get("epr").get(0), false, true);
    }

    @Async
    public CompletableFuture<EpisodesForTitle> searchEpisodes_orig(EpisodesForTitle episodesForTitle) {
        Elements showEpisodesAnchors = Jsoup.parse("").select(".episodes.range li a");

        Element showParent = showEpisodesAnchors.first();
        String showName = showParent.select("h2.film-name").text();

        List<VideoSearchResult> showSplashPageWatchButtons = showParent
            .select("a.btn-play")
            .stream()
            .map(element -> new VideoSearchResult(
                getUrlWithOrigin(element.attr("href")),
                showName
            ))
            .collect(Collectors.toList());

        if (showSplashPageWatchButtons == null || showSplashPageWatchButtons.size() == 0) {
            return null;
        }

        String showWatchUrl = getUrlWithOrigin(showSplashPageWatchButtons.get(0).getUrl());

        log.info("Attempting to request: {}", showWatchUrl);

        String showHtml = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(showWatchUrl),
            URI.create(ORIGIN),
            null,
            getSearchHeaders()
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

    public VideoSearchResult getVideosForShow_orig(String url) {
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
