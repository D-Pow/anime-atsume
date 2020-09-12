package org.animeatsume.api.service;

import lombok.extern.slf4j.Slf4j;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.TitlesAndEpisodes.EpisodesForTitle;
import org.animeatsume.api.model.VideoSearchResult;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.http.Requests;
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

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FourAnimeService {
    private static final String ORIGIN = "https://4anime.to";
    private static final String SEARCH_URL = ORIGIN + "/wp-admin/admin-ajax.php";
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
        headers.set("User-Agent", mockFirefoxUserAgent);
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
            getNecessaryRequestHeaders(),
            false
        ).getBody();

        if (episodeHtml != null) {
            Element videoSource = Jsoup.parse(episodeHtml)
                .select(EPISODE_VIDEO_SOURCE_SELECTOR)
                .first();

            return new VideoSearchResult(videoSource.attr("src"), null, true);
        }

        return null;
    }
}
