package org.animeatsume.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.animeatsume.api.model.TitleSearchRequest;
import org.animeatsume.api.model.TitlesEpisodesSearchResults;
import org.animeatsume.api.service.FourAnimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FourAnimeController {
    @Autowired
    FourAnimeService fourAnimeService;

    public TitlesEpisodesSearchResults searchTitle(TitleSearchRequest request) {
        TitlesEpisodesSearchResults titleResults = fourAnimeService.searchTitle(request.getTitle());

        return titleResults;
    }
}
