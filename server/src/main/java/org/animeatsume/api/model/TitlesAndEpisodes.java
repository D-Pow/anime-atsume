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
public class TitlesAndEpisodes {
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class EpisodesForTitle extends VideoSearchResult {
        private List<VideoSearchResult> episodes = new ArrayList<>();

        private static List<VideoSearchResult> mapAnchorListToVideoSearchResult(List<Anchor> episodes, boolean isDirectSource) {
            return episodes.stream()
                .map(anchor -> new VideoSearchResult(anchor.getUrl(), anchor.getTitle(), isDirectSource))
                .collect(Collectors.toList());
        }

        public EpisodesForTitle(String url, String title) {
            this(url, title, new ArrayList<>(), false);
        }

        public EpisodesForTitle(String url, String title, List<Anchor> episodes, boolean areVideosDirectSource) {
            super(url, title, false);
            setEpisodes(episodes, areVideosDirectSource);
        }

        public EpisodesForTitle(String url, String title, List<VideoSearchResult> episodes) {
            super(url, title, false);
            this.episodes = episodes;
        }

        public void setEpisodes(List<Anchor> episodes, boolean areVideosDirectSource) {
            this.episodes = mapAnchorListToVideoSearchResult(episodes, areVideosDirectSource);
        }
    }

    private List<EpisodesForTitle> results;
}
