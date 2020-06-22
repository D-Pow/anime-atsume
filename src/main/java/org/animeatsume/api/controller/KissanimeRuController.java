package org.animeatsume.api.controller;

import org.animeatsume.api.model.*;
import org.animeatsume.api.service.KissanimeRuService;
import org.animeatsume.api.service.NovelPlanetService;
import org.animeatsume.api.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class KissanimeRuController {
    private static final Logger log = LoggerFactory.getLogger(KissanimeRuController.class);

    @Autowired
    KissanimeRuService kissanimeService;

    @Autowired
    NovelPlanetService novelPlanetService;

    public KissanimeSearchResponse searchKissanimeTitles(KissanimeSearchRequest kissanimeSearchRequest) {
        KissanimeSearchResponse kissanimeSearchResponse = kissanimeService.searchKissanimeTitles(kissanimeSearchRequest);
        List<KissanimeSearchResponse.SearchResults> titleSearchResults = kissanimeSearchResponse.getResults();

        if (titleSearchResults != null) {
            List<CompletableFuture<List<Anchor>>> episodeSearchResultsFutures = new ArrayList<>();

            titleSearchResults.forEach(titleAnchor -> {
                episodeSearchResultsFutures.add(kissanimeService.searchKissanimeEpisodes(titleAnchor.getUrl()));
            });

            ObjectUtils.getAllCompletableFutureResults(episodeSearchResultsFutures, (episodeAnchorList, index) -> {
                KissanimeSearchResponse.SearchResults episodeSearchResult = titleSearchResults.get(index);
                List<Anchor> episodeLinks = new ArrayList<>();

                if (episodeAnchorList != null) {
                    episodeLinks = episodeAnchorList;
                }

                episodeSearchResult.setEpisodes(episodeLinks);
            });
        }

        return kissanimeSearchResponse;
    }

    public ResponseEntity<Object> getVideosForKissanimeEpisode(KissanimeVideoHostRequest request) {
        KissanimeVideoHostResponse videoHost = getVideoHostUrlForKissanimeEpisode(request);
        String videoHostUrl = videoHost.getVideoHostUrl();
        Object body = videoHost;

        if (videoHostUrl != null && !videoHostUrl.isEmpty() && videoHostUrl.contains(NovelPlanetService.DOMAIN)) {
            body = getVideoSourcesForNovelPlanetHost(videoHostUrl);
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(body);
    }

    private KissanimeVideoHostResponse getVideoHostUrlForKissanimeEpisode(KissanimeVideoHostRequest request) {
        log.info("KissanimeVideoHostRequest = {}", request);

        String kissanimeEpisodeUrl = request.getEpisodeUrl();
        String captchaAnswer = request.getCaptchaAnswer();

        if (captchaAnswer == null || captchaAnswer.equals("")) {
            if (kissanimeService.requestIsRedirected(kissanimeEpisodeUrl)) {
                // Request is redirected because AreYouHuman verification needs to be completed
                // TODO img src URLs are inaccessible if the user doesn't have the Cloudflare
                //  cookie set. Either download images here or set cookie in response.
                return kissanimeService.getBypassAreYouHumanPromptContent(kissanimeEpisodeUrl);
            }

            return kissanimeService.getVideoHostUrlFromEpisodePage(kissanimeEpisodeUrl);
        }

        boolean bypassSuccess = kissanimeService.executeBypassAreYouHumanCheck(kissanimeEpisodeUrl, captchaAnswer);

        if (!bypassSuccess) {
            return kissanimeService.getBypassAreYouHumanPromptContent(kissanimeEpisodeUrl);
        }

        return kissanimeService.getVideoHostUrlFromEpisodePage(kissanimeEpisodeUrl);
    };

    public NovelPlanetSourceResponse getVideoSourcesForNovelPlanetHost(String videoHostUrl) {
        log.info("Getting NovelPlanet MP4 sources for ({})", videoHostUrl);
        URI videoHostUri = URI.create(videoHostUrl);

        HttpEntity<Void> corsEntity = novelPlanetService.getCorsEntityForNovelPlanet(null, videoHostUri);
        String novelPlanetApiUrl = novelPlanetService.getApiUrlForHost(videoHostUri);
        NovelPlanetSourceResponse sourcesForVideo = novelPlanetService.getRedirectorSourcesForVideo(novelPlanetApiUrl, corsEntity);

        List<CompletableFuture<Void>> mp4UrlCompletableFutures = sourcesForVideo.getData().stream()
            .map(novelPlanetSource -> novelPlanetService.getMp4UrlFromRedirectorUrl(novelPlanetSource, corsEntity))
            .collect(Collectors.toList());

        ObjectUtils.getAllCompletableFutureResults(mp4UrlCompletableFutures);

        return sourcesForVideo;
    }
}
