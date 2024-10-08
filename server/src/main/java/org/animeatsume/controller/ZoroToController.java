package org.animeatsume.controller;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.model.TitleSearchRequest;
import org.animeatsume.model.TitlesAndEpisodes;
import org.animeatsume.model.VideoSearchResult;
import org.animeatsume.service.ZoroToService;
import org.animeatsume.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Controller
public class ZoroToController implements ShowSearchController {
    @Autowired
    ZoroToService zoroToService;

    public TitlesAndEpisodes searchShows(TitleSearchRequest request) {
        TitlesAndEpisodes titleResults = null;

        try {
            titleResults = zoroToService.searchShows(request.getTitle());
            log.info("titleResults: {}", titleResults);

            if (titleResults != null) {
                List<CompletableFuture<TitlesAndEpisodes.EpisodesForTitle>> episodeSearchFutures = titleResults.getResults().stream()
                    .map(titleResult -> zoroToService.searchEpisodes((TitlesAndEpisodes.EpisodesForTitle) titleResult))
                    .collect(Collectors.toList());

                List<TitlesAndEpisodes.EpisodesForTitle> allCompletableFutureResults =
                    ObjectUtils.getAllCompletableFutureResults(episodeSearchFutures);

                log.info(allCompletableFutureResults);
            }
        } catch (Exception e) {
            log.error("Exception trying to serach {}:", ZoroToService.ORIGIN, e);
        }

        return titleResults;
    }

    public TitlesAndEpisodes.EpisodesForTitle getVideosForEpisode(String url) {
        VideoSearchResult video = zoroToService.getVideosForShow(url);

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
