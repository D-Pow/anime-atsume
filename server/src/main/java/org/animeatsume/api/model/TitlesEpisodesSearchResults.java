package org.animeatsume.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TitlesEpisodesSearchResults {
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TitleResults extends VideoSearchResult {
        private List<VideoSearchResult> episodes;

        private static List<VideoSearchResult> mapAnchorListToVideoSearchResult(List<Anchor> episodes, boolean isDirectSource) {
            return episodes.stream()
                .map(anchor -> new VideoSearchResult(anchor.getUrl(), anchor.getTitle(), isDirectSource))
                .collect(Collectors.toList());
        }

        public TitleResults(String url, String title) {
            this(url, title, new ArrayList<>(), false);
        }

        public TitleResults(String url, String title, List<Anchor> episodes, boolean areVideosDirectSource) {
            super(url, title, false);
            setEpisodes(episodes, areVideosDirectSource);
        }

        public void setEpisodes(List<Anchor> episodes, boolean areVideosDirectSource) {
            this.episodes = mapAnchorListToVideoSearchResult(episodes, areVideosDirectSource);
        }
    }

    private List<TitleResults> results;
}
