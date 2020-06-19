package org.animeatsume.api.controller;

import org.animeatsume.api.model.Anchor;
import org.animeatsume.api.model.KissanimeSearchRequest;
import org.animeatsume.api.model.KissanimeSearchResponse;
import org.animeatsume.api.model.KissanimeVideoHostRequest;
import org.animeatsume.api.service.KissanimeRuService;
import org.animeatsume.api.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class KissanimeRuController {
    private static final Logger log = LoggerFactory.getLogger(KissanimeRuController.class);

    @Autowired
    KissanimeRuService kissanimeService;

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

    public void getNovelPlanetUrlForKissanimeEpisode(KissanimeVideoHostRequest request) {
        String kissanimeEpisodeUrl = request.getEpisodeUrl();

        if (kissanimeService.requestIsRedirected(kissanimeEpisodeUrl)) {
            // Request is redirected because AreYouHuman verification needs to be completed
            List<KissanimeRuService.BypassAreYouHumanCheckRequestFields> bypassAttemptConfigs =
                kissanimeService.getAllBypassAreYouHumanConfigurations(kissanimeEpisodeUrl);

            List<CompletableFuture<String>> bypassAttempts = new ArrayList<>();

            bypassAttemptConfigs.forEach(config -> {
                bypassAttempts.add(kissanimeService.executeBypassAreYouHumanCheck(config));
            });

            List<String> attemptResults = ObjectUtils.getAllCompletableFutureResults(bypassAttempts);

            List<String> successfulBypassHtmlStrings = attemptResults.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            log.info("Successful bypass HTML strings = {}", successfulBypassHtmlStrings);
        }
    };
}
