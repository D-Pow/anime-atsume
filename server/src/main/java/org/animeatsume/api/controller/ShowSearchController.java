package org.animeatsume.api.controller;

import org.animeatsume.api.model.TitleSearchRequest;
import org.animeatsume.api.model.TitlesAndEpisodes;

public interface ShowSearchController {
    TitlesAndEpisodes searchShows(TitleSearchRequest request);
    TitlesAndEpisodes.EpisodesForTitle getVideosForEpisode(String url);
}
