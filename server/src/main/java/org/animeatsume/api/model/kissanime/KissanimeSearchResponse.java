package org.animeatsume.api.model.kissanime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.animeatsume.api.model.Anchor;
import org.animeatsume.api.model.VideoSearchResult;

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

        private static List<VideoSearchResult> mapAnchorListToVideoSearchResult(List<Anchor> episodes) {
            return episodes.stream()
                .map(anchor -> new VideoSearchResult(anchor.getUrl(), anchor.getTitle(), false))
                .collect(Collectors.toList());
        }

        public SearchResults(String url, String title) {
            super(url, title, false);
        }

        public SearchResults(String url, String title, List<Anchor> episodes) {
            super(url, title, false);
            this.episodes = mapAnchorListToVideoSearchResult(episodes);
        }

        public void setEpisodes(List<Anchor> episodes) {
            this.episodes = mapAnchorListToVideoSearchResult(episodes);
        }
    }

    private List<SearchResults> results;
}
