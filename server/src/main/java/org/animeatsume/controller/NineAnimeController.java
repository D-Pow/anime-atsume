package org.animeatsume.controller;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.model.SearchAnimeResponse;
import org.animeatsume.model.TitleSearchRequest;
import org.animeatsume.model.TitlesAndEpisodes;
import org.animeatsume.model.VideoSearchResult;
import org.animeatsume.service.NineAnimeService;
import org.animeatsume.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Controller
public class NineAnimeController implements ShowSearchController {
    @Autowired
    NineAnimeService nineAnimeService;

    public SearchAnimeResponse searchShows(TitleSearchRequest request) {
        SearchAnimeResponse titleResults = null;

        try {
            titleResults = new SearchAnimeResponse(nineAnimeService.searchShows(request.getTitle()));

            if (titleResults != null) {
                List<CompletableFuture<TitlesAndEpisodes.EpisodesForTitle>> episodeSearchFutures = titleResults.getResults().stream()
                    .map(titleResult -> nineAnimeService.searchEpisodes((TitlesAndEpisodes.EpisodesForTitle) titleResult))
                    .collect(Collectors.toList());

                ObjectUtils.getAllCompletableFutureResults(episodeSearchFutures);
            }
        } catch (Exception e) {
            log.error("Exception trying to serach 9anime:", e);
        }

        return titleResults;
    }

    public TitlesAndEpisodes.EpisodesForTitle getVideosForEpisode(String url) {
        VideoSearchResult video = nineAnimeService.getVideosForEpisode(url);

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
