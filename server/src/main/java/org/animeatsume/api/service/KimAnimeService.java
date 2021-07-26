package org.animeatsume.api.service;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class KimAnimeService {
    private static final String ORIGIN = "https://kimanime.com";
    private static final String SEARCH_URL = ORIGIN + "/api/anime/get/list";
    private static final List<String> DIRECT_SOURCE_VIDEO_ORIGINS = Arrays.asList("https://storage.googleapis.com", "https://[^\\.]+.4animu.me");
    private static final String TITLE_ANCHOR_SELECTOR = "a.name";
    private static final String EPISODE_ANCHOR_SELECTOR = "ul.episodes a[title]";
    private static final String EPISODE_VIDEO_SOURCE_SELECTOR = "video source";

    @Value("${org.animeatsume.mock-firefox-user-agent}")
    private String mockFirefoxUserAgent;

    @Async
    public CompletableFuture<Void> searchEpisodes(EpisodesForTitle episodesForTitle) {
        //
    }
}
