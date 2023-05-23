package org.animeatsume.api.service;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.TitlesAndEpisodes.EpisodesForTitle;
import org.animeatsume.api.model.VideoSearchResult;
import org.animeatsume.api.model.ZoroToShowResponse;
import org.animeatsume.api.utils.ObjectUtils;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.http.Requests;
import org.animeatsume.api.utils.regex.RegexUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ZoroToService {
    private static final String ORIGIN = "https://zoro.to";
    private static final String SEARCH_URL = ORIGIN + "/ajax/search/suggest?keyword=";
    private static final String EPISODES_SELECTOR = "#episodes-content a";
    private static final String DOWNLOAD_ANCHOR_SELECTOR = "a.pc-download";  // The "Download" link that redirects to a different page
    private static final String DOWNLOAD_BUTTON_SELECTOR = ".dowload a";  // The different download links of varying video resolutions. Yes, they made a typo
    private static final String EPISODE_VIDEO_SOURCE_SELECTOR = "video source";

    @Value("${org.animeatsume.mock-firefox-user-agent}")
    private String mockFirefoxUserAgent;

    private static HttpHeaders getSearchHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.ACCEPT, "text/html");

        return headers;
    }

    private static String getUrlWithOrigin(String... suffixes) {
        List<String> urlPaths = new ArrayList<>(Arrays.asList(suffixes));
        urlPaths.add(0, ORIGIN);
        String joinedUrl = String.join("/", urlPaths).replaceAll("[\\\\/]\"(?=(\\\\|/|$))", "");

        return joinedUrl;
    }

    public TitlesAndEpisodes searchShows(String title) {
        log.info("Searching <{}> for title ({}) ...", ORIGIN, title);

        Object searchResponse = CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(SEARCH_URL + title),
            URI.create(ORIGIN),
            null,
            getSearchHeaders()
        ).getBody();

        ZoroToShowResponse searchResponseObject;

        try {
            searchResponseObject = ZoroToShowResponse.fromString((String) searchResponse);
        } catch (Exception responseIsMapInsteadOfString) {
            searchResponseObject = ZoroToShowResponse.fromMap((Map<String, Object>) searchResponse);
        }
        String searchResponseHtml = searchResponseObject.getHtml();

        if (searchResponseHtml != null) {
            Document showResultsDocument = Jsoup.parse(searchResponseHtml);
            Elements showAnchors = showResultsDocument.select("a");
            List<EpisodesForTitle> showResults = showAnchors
                .stream()
                .map(element -> {
                    Element showTitleElement = element.selectFirst("h3.film-name");
                    String showTitle = showTitleElement != null
                        ? showTitleElement.text()
                        : null;

                    EpisodesForTitle show = showTitle != null
                        ? new EpisodesForTitle(
                            getUrlWithOrigin(element.attr("href")),
                            showTitle
                        )
                        : null;

                    return show;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            log.info("Obtained {} show(s) for ({})", showResults.size(), title);

            return new TitlesAndEpisodes(showResults);
        }

        return null;
    }

    @Async
    public CompletableFuture<EpisodesForTitle> searchEpisodes(EpisodesForTitle episodesForTitle) {
        log.info("Searching <{}> for episode list at ({}) ...", ORIGIN, episodesForTitle.getUrl());

        String showSplashPage = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(episodesForTitle.getUrl()),
            URI.create(ORIGIN),
            null,
            getSearchHeaders()
        ).getBody();

        Element watchNowButton = Jsoup.parse(showSplashPage).select(".film-buttons .btn-play").first();

        if (watchNowButton == null) {
            return null;
        }

        String showEpisodesPageHtml = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(getUrlWithOrigin(watchNowButton.attr("href"))),
            URI.create(ORIGIN),
            null,
            getSearchHeaders()
        ).getBody();

        // TODO - HTML doesn't have the episodes until JavaScript injects elements onto the page
        // log.info("showEpisodesPageHtml: {}", showEpisodesPageHtml);

        Document showEpisodesPageDom = Jsoup.parse(showEpisodesPageHtml);

        List<VideoSearchResult> episodes = showEpisodesPageDom.select(".ssl-item.ep-item").stream()
            .map(anchor -> {
                String showUrl = anchor.attr("href");
                String showTitle = anchor.attr("title");

                return new VideoSearchResult(showUrl, showTitle);
            })
            .filter(videoSearchResult ->(
                videoSearchResult.getUrl() != null
                && videoSearchResult.getUrl().length() > 0
                && videoSearchResult.getTitle() != null
                && videoSearchResult.getTitle().length() > 0
            ))
            .collect(Collectors.toList());

        episodesForTitle.setEpisodes(episodes);

        return CompletableFuture.completedFuture(episodesForTitle);

//        String showWatchUrl = getUrlWithOrigin(showSplashPageWatchButtons.get(0).getUrl());
//
//        log.info("Attempting to request: {}", showWatchUrl);
//
//        String showHtml = (String) CorsProxy.doCorsRequest(
//            HttpMethod.GET,
//            URI.create(showWatchUrl),
//            URI.create(ORIGIN),
//            null,
//            getSearchHeaders()
//        ).getBody();
//
//        if (showHtml == null || showHtml.length() == 0) {
//            return null;
//        }
//
//        List<VideoSearchResult> episodeAnchors = Jsoup.parse(showHtml)
//            .select(EPISODES_SELECTOR)
//            .stream()
//            .map(element -> new VideoSearchResult(
//                String.format("%s/%s", ORIGIN, element.attr("href")),
//                element.attr("title")
//            ))
//            .collect(Collectors.toList());
//
//        log.info("Obtained {} episodes for ({})",
//            episodesForTitle.getEpisodes().size(),
//            episodesForTitle.getTitle()
//        );
//
//        getDirectVideoUrls(episodeAnchors);
//
//        log.info("Determined URLs for {} episodes for ({})",
//            episodesForTitle.getEpisodes().size(),
//            episodesForTitle.getTitle()
//        );
//
//        return CompletableFuture.completedFuture(episodesForTitle);
    }

    public List<VideoSearchResult> getDirectVideoUrls(List<VideoSearchResult> episodeAnchors) {
        List<CompletableFuture<VideoSearchResult>> allEpisodesAndDownloadUrls = episodeAnchors
            .stream()
            .map(episodeAnchor -> {
                log.info("Getting zoro.to MP4 video from ({})", episodeAnchor.getUrl());

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
            getSearchHeaders(),
            false
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

                log.info("zoro.to video src not in video.src property [{}], rather in index.html [{}]",
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
