package org.animeatsume.controller;

import org.animeatsume.model.TitleSearchRequest;
import org.animeatsume.model.TitlesAndEpisodes;

public interface ShowSearchController {
    TitlesAndEpisodes searchShows(TitleSearchRequest request);
    TitlesAndEpisodes.EpisodesForTitle getVideosForEpisode(String url);
}
