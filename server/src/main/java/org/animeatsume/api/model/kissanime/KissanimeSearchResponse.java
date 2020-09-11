package org.animeatsume.api.model.kissanime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.animeatsume.api.model.Anchor;
import org.animeatsume.api.model.VideoSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KissanimeSearchResponse {
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SearchResults extends VideoSearchResult {
        private List<VideoSearchResult> episodes;

        private static List<VideoSearchResult> mapAnchorListToVideoSearchResult(List<Anchor> episodes, boolean isDirectSource) {
            return episodes.stream()
                .map(anchor -> new VideoSearchResult(anchor.getUrl(), anchor.getTitle(), isDirectSource))
                .collect(Collectors.toList());
        }

        public SearchResults(String url, String title) {
            this(url, title, new ArrayList<>(), false);
        }

        public SearchResults(String url, String title, List<Anchor> episodes, boolean areVideosDirectSource) {
            super(url, title, areVideosDirectSource);
            setEpisodes(episodes, areVideosDirectSource);
        }

        public void setEpisodes(List<Anchor> episodes) {
            setEpisodes(episodes, false);
        }

        public void setEpisodes(List<Anchor> episodes, boolean areVideosDirectSource) {
            this.episodes = mapAnchorListToVideoSearchResult(episodes, areVideosDirectSource);
        }
    }

    private List<SearchResults> results;
}
