package org.animeatsume.api.service;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.model.Anchor;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.TitlesAndEpisodes.EpisodesForTitle;
import org.animeatsume.api.model.VideoSearchResult;
import org.animeatsume.api.model.ZoroToEpisodes;
import org.animeatsume.api.model.ZoroToShowResponse;
import org.animeatsume.api.model.ZoroToStreamingSource;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ZoroToService {
    public static final String ORIGIN = "https://zoro.to";
    private static final String SEARCH_URL = ORIGIN + "/ajax/search/suggest?keyword=";
    private static final String EPISODES_SELECTOR = "#episodes-content a";
    private static final String DOWNLOAD_ANCHOR_SELECTOR = "a.pc-download";  // The "Download" link that redirects to a different page
    private static final String DOWNLOAD_BUTTON_SELECTOR = ".dowload a";  // The different download links of varying video resolutions. Yes, they made a typo
    private static final String EPISODE_VIDEO_SOURCE_SELECTOR = "video source";

    @Value("${org.animeatsume.mock-firefox-user-agent}")
    private String mockFirefoxUserAgent;

    private static HttpHeaders getSearchHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);

        return headers;
    }

    private static String getUrlWithOrigin(String... suffixes) {
        List<String> urlPaths = new ArrayList<>(Arrays.asList(suffixes));
        urlPaths.add(0, ORIGIN);
        String joinedUrl = String.join("/", urlPaths).replaceAll("[\\\\/]\"(?=(\\\\|/|$))", "");

        return joinedUrl;
    }

    private static String getSearchUrl(String query) {
        return SEARCH_URL + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    public TitlesAndEpisodes searchShows(String title) {
        log.info("Searching <{}> for title ({}) ...", ORIGIN, title);

        Object searchResponse = CorsProxy.doCorsRequest(
            HttpMethod.GET,
            getSearchUrl(title),
            ORIGIN,
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

                    if (show != null) {
                        // Filter out null search results
                        show.setEpisodes(
                            show.getEpisodes().stream()
                                .filter(episode -> episode.getUrl() != null && !episode.getUrl().isEmpty())
                                .toList()
                        );
                    }

                    return show;
                })
                // Remove null show search results
                .filter(Objects::nonNull)
                // Remove empty show search results
                .filter(show -> show.getEpisodes().size() > 0)
                .collect(Collectors.toList());

            log.info("Obtained {} show(s) for ({})", showResults.size(), title);

            if (showResults.size() <= 0) {
                return null;
            }

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

        if (showSplashPage == null) {
            return CompletableFuture.completedFuture(episodesForTitle);
        }

        Element watchNowButton = Jsoup.parse(showSplashPage).select(".film-buttons .btn-play").first();

        if (watchNowButton == null) {
            return CompletableFuture.completedFuture(episodesForTitle);
        }

        // String showEpisodesPageHtml = (String) CorsProxy.doCorsRequest(
        //     HttpMethod.GET,
        //     URI.create(getUrlWithOrigin(watchNowButton.attr("href"))),
        //     URI.create(ORIGIN),
        //     null,
        //     getSearchHeaders()
        // ).getBody();
        //
        // if (showEpisodesPageHtml == null || showEpisodesPageHtml.isEmpty()) {
        //     return CompletableFuture.completedFuture(episodesForTitle);
        // }

        // ResponseEntity<?> streamingIframesResponse = CorsProxy.doCorsRequest(
        //     HttpMethod.GET,
        //     URI.create("https://zoro.to/ajax/v2/episode/servers?episodeId=" + watchNowButton.attr("href")),
        //     URI.create(ORIGIN),
        //     null,
        //     getSearchHeaders()
        // );


        String showId = RegexUtils.getFirstMatchGroups("\\d+$", watchNowButton.attr("href")).get(0);

        // HttpHeaders serversAndEpisodesRequestHeaders = new HttpHeaders();
        // serversAndEpisodesRequestHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        ResponseEntity<?> serversAndEpisodesResponse = CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(ORIGIN + "/ajax/v2/episode/list/" + showId),
            URI.create(ORIGIN),
            null,
            getSearchHeaders()
        );

        List<Anchor> episodesPages = ZoroToEpisodes.fromString((String) serversAndEpisodesResponse.getBody()).getWatchAnchors(ORIGIN);

        episodesForTitle.setEpisodes(episodesPages, false);

        // TODO - HTML doesn't have the episodes until JavaScript injects elements onto the page
        //  I think it goes through Google Tag manager but it's so obfuscated, it's hard to tell
//        List<CompletableFuture<VideoSearchResult>> directVideoUrls = getDirectVideoUrls(episodesForTitle.getEpisodes());
//
//        ObjectUtils.getAllCompletableFutureResults(directVideoUrls);

        return CompletableFuture.completedFuture(episodesForTitle);
    }

    @Async
    public List<CompletableFuture<VideoSearchResult>> getDirectVideoUrls(List<VideoSearchResult> episodeAnchors) {
        List<CompletableFuture<VideoSearchResult>> allEpisodesAndDownloadUrls = episodeAnchors
            .stream()
            .map(episodeAnchor -> {
                String episodeId = RegexUtils.getFirstMatchGroups("\\d+$", episodeAnchor.getUrl()).get(0);
                String episodeUrl = ORIGIN + "/ajax/v2/episode/sources?id=" + episodeId;

                log.info("Getting zoro.to MP4 video from ({})", episodeUrl);

                String streamHostResponse = (String) CorsProxy.doCorsRequest(
                    HttpMethod.GET,
                    URI.create(episodeUrl),
                    URI.create(ORIGIN),
                    null,
                    getSearchHeaders()
                ).getBody();

                ZoroToStreamingSource streamingSource = ZoroToStreamingSource.fromString(streamHostResponse);

                log.info("streamingSource for URL <{}>: {}", episodeUrl, streamingSource);
                // TODO - Find out how to get actual .mp4 URL from rapid-cloud.co
                // episodeAnchor.setUrl(highestQualityVideo.getUrl());
                // JS:
                // (await (await fetch('https://rapid-cloud.co/ajax/embed-6/getSources?id=tQVDjQoOgVKg')).json())
                // (await (await fetch('https://rapid-cloud.co/ajax/embed-6/getSources?id=tQVDjQoOgVKg')).json()).sourcesBackup
                // (await (await fetch('https://zoro.to/ajax/v2/episode/sources?id=12355')).json())

                return CompletableFuture.completedFuture(episodeAnchor);
            })
            .collect(Collectors.toList());

        return allEpisodesAndDownloadUrls;
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
