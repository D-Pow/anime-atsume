package org.animeatsume.api.controller;

import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class FourAnimeController {
    @Autowired
    FourAnimeService fourAnimeService;

    public TitlesAndEpisodes searchTitle(TitleSearchRequest request) {
        TitlesAndEpisodes titleResults = null;

        try {
            titleResults = fourAnimeService.searchTitle(request.getTitle());
        } catch (Exception e) {
            // 4anime is not working
        }

        if (titleResults != null) {
            List<CompletableFuture<Void>> episodeSearchFutures = titleResults.getResults().stream()
                .map(titleResult -> fourAnimeService.searchEpisodes(titleResult))
                .collect(Collectors.toList());

            ObjectUtils.getAllCompletableFutureResults(episodeSearchFutures);
        }

        return titleResults;
    }

    public TitlesAndEpisodes.EpisodesForTitle getVideoForEpisode(String url) {
        VideoSearchResult video = null;

        try {
            video = fourAnimeService.getVideoForEpisode(url);
        } catch (Exception e) {
            log.error("Could not fetch video for episode from <{}>", url);
        }

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
