package org.animeatsume.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KissanimeSearchResponse {
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SearchResults extends Anchor {
        private List<Anchor> episodes;

        public SearchResults(String url, String title) {
            super(url, title);
        }

        public SearchResults(String url, String title, List<Anchor> episodes) {
            super(url, title);
            this.episodes = episodes;
        }
    }

    private List<SearchResults> results;
}
