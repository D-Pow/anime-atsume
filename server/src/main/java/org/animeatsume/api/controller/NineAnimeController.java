package org.animeatsume.api.controller;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.model.TitleSearchRequest;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.VideoSearchResult;
import org.animeatsume.api.service.NineAnimeService;
import org.animeatsume.api.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Controller
public class NineAnimeController {
    @Autowired
    NineAnimeService nineAnimeService;

    public TitlesAndEpisodes searchShows(TitleSearchRequest request) {
        TitlesAndEpisodes titleResults = nineAnimeService.searchShows(request.getTitle());

        if (titleResults != null) {
            List<CompletableFuture<TitlesAndEpisodes.EpisodesForTitle>> episodeSearchFutures = titleResults.getResults().stream()
                .map(titleResult -> nineAnimeService.searchEpisodes(titleResult))
                .collect(Collectors.toList());

            ObjectUtils.getAllCompletableFutureResults(episodeSearchFutures);
        }

        return titleResults;
    }

    public TitlesAndEpisodes.EpisodesForTitle getVideoForEpisode(String url) {
        VideoSearchResult video = nineAnimeService.getVideosForShow(url);

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
