package org.animeatsume.api.service;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.TitlesAndEpisodes.EpisodesForTitle;
import org.animeatsume.api.model.VideoSearchResult;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Service
public class FourAnimeService {
    private static final String ORIGIN = "https://4anime.to";
    private static final String SEARCH_URL = ORIGIN + "/wp-admin/admin-ajax.php";
    private static final List<String> DIRECT_SOURCE_VIDEO_ORIGINS = Arrays.asList("https://storage.googleapis.com", "https://[^\\.]+.4animu.me");
    private static final String TITLE_ANCHOR_SELECTOR = "a.name";
    private static final String EPISODE_ANCHOR_SELECTOR = "ul.episodes a[title]";
    private static final String EPISODE_VIDEO_SOURCE_SELECTOR = "video source";

    @Value("${org.animeatsume.mock-firefox-user-agent}")
    private String mockFirefoxUserAgent;

    private static String[][] getTitleSearchHttpEntity(String title) {
        return new String[][] {
            { "action", "ajaxsearchlite_search" },
            { "asid", "1" },
            { "options", "qtranslate_lang=0&set_intitle=None&customset%5B%5D=anime" },
            { "aslp", title }
        };
    }

    private HttpHeaders getNecessaryRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, mockFirefoxUserAgent);
        headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.ALL));
        return headers;
    }

    public TitlesAndEpisodes searchTitle(String title) {
        log.info("Searching 4anime for title ({}) ...", title);

        String[][] titleSearchFormData = getTitleSearchHttpEntity(title);
        HttpEntity titleSearchHttpEntity = Requests.getFormDataHttpEntity(getNecessaryRequestHeaders(), titleSearchFormData);

        String searchResponseHtml = (String) CorsProxy.doCorsRequest(
            HttpMethod.POST,
            URI.create(SEARCH_URL),
            URI.create(ORIGIN),
            titleSearchHttpEntity.getBody(),
            titleSearchHttpEntity.getHeaders()
        ).getBody();

        if (searchResponseHtml != null) {
            Document titleResultsDocument = Jsoup.parse(searchResponseHtml);
            Elements titleAnchors = titleResultsDocument.select(TITLE_ANCHOR_SELECTOR);

            log.info("Obtained {} show(s) for ({})", titleAnchors.size(), title);

            return new TitlesAndEpisodes(titleAnchors.stream()
                .map(element -> new EpisodesForTitle(
                    element.attr("href"),
                    element.text()
                ))
                .collect(Collectors.toList()));
        }

        return null;
    }

    @Async
    public CompletableFuture<Void> searchEpisodes(EpisodesForTitle episodesForTitle) {
        log.info("Searching 4anime for episode list at ({}) ...", episodesForTitle.getUrl());

        String showHtml = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(episodesForTitle.getUrl()),
            URI.create(ORIGIN),
            null,
            getNecessaryRequestHeaders()
        ).getBody();

        if (showHtml != null) {
            List<VideoSearchResult> episodeAnchors = Jsoup.parse(showHtml)
                .select(EPISODE_ANCHOR_SELECTOR)
                .stream()
                .map(element -> new VideoSearchResult(
                    element.attr("href"),
                    element.text()
                ))
                .collect(Collectors.toList());

            episodesForTitle.setEpisodes(episodeAnchors);
        }

        log.info("Obtained {} episodes for ({})",
            episodesForTitle.getEpisodes().size(),
            episodesForTitle.getTitle()
        );

        return CompletableFuture.completedFuture(null);
    }

    public VideoSearchResult getVideoForEpisode(String url) {
        log.info("Getting 4anime MP4 video from ({})", url);

        String episodeHtml = (String) CorsProxy.doCorsRequest(
            HttpMethod.GET,
            URI.create(url),
            URI.create(ORIGIN),
            null,
            getNecessaryRequestHeaders()
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

                log.info("4anime video src not in video.src property [{}], rather in index.html [{}]",
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
        return "(" + String.join("|", DIRECT_SOURCE_VIDEO_ORIGINS) + ")";
    }
}
