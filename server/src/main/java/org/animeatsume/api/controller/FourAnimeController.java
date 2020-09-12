package org.animeatsume.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.animeatsume.api.model.TitleSearchRequest;
import org.animeatsume.api.model.TitlesEpisodesSearchResults;
import org.animeatsume.api.service.FourAnimeService;
import org.animeatsume.api.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FourAnimeController {
    @Autowired
    FourAnimeService fourAnimeService;

    public TitlesEpisodesSearchResults searchTitle(TitleSearchRequest request) {
        TitlesEpisodesSearchResults titleResults = fourAnimeService.searchTitle(request.getTitle());

        if (titleResults != null) {
            List<CompletableFuture<Void>> episodeSearchFutures = titleResults.getResults().stream()
                .map(titleResult -> fourAnimeService.searchEpisodes(titleResult))
                .collect(Collectors.toList());

            ObjectUtils.getAllCompletableFutureResults(episodeSearchFutures);
        }

        return titleResults;
    }
}
