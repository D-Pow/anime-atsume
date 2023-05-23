package org.animeatsume.api.controller;

import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.model.TitleSearchRequest;
import org.animeatsume.api.model.TitlesAndEpisodes;
import org.animeatsume.api.model.VideoSearchResult;
import org.animeatsume.api.service.ZoroToService;
import org.animeatsume.api.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log4j2
@Controller
public class ZoroToController {
    @Autowired
    ZoroToService zoroToService;

    public TitlesAndEpisodes searchShows(TitleSearchRequest request) {
        TitlesAndEpisodes titleResults = null;

        try {
            titleResults = zoroToService.searchShows(request.getTitle());
            log.info("titleResults: {}", titleResults);

            if (titleResults != null) {
                List<CompletableFuture<TitlesAndEpisodes.EpisodesForTitle>> episodeSearchFutures = titleResults.getResults().stream()
                    .map(titleResult -> zoroToService.searchEpisodes(titleResult))
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

    public TitlesAndEpisodes.EpisodesForTitle getVideoForEpisode(String url) {
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
