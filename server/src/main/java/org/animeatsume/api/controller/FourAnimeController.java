package org.animeatsume.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.animeatsume.api.model.TitleSearchRequest;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.VideoSearchResult;
import org.animeatsume.api.service.FourAnimeService;
import org.animeatsume.api.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class FourAnimeController {
    @Autowired
    FourAnimeService fourAnimeService;

    public TitlesAndEpisodes searchTitle(TitleSearchRequest request) {
        TitlesAndEpisodes titleResults = fourAnimeService.searchTitle(request.getTitle());

        if (titleResults != null) {
            List<CompletableFuture<Void>> episodeSearchFutures = titleResults.getResults().stream()
                .map(titleResult -> fourAnimeService.searchEpisodes(titleResult))
                .collect(Collectors.toList());

            ObjectUtils.getAllCompletableFutureResults(episodeSearchFutures);
        }

        return titleResults;
    }

    public TitlesAndEpisodes.EpisodesForTitle getVideoForEpisode(String url) {
        VideoSearchResult video = fourAnimeService.getVideoForEpisode(url);

        if (video == null) {
            return null;
        }

        return new TitlesAndEpisodes.EpisodesForTitle(
            url,
            null,
            Arrays.asList(video)
        );
    }
}
