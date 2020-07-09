package org.animeatsume.api.model.himovies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HiMoviesSearchResponse {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchResult {
        private String title;
        private String showUrl;
        private String imgSrc;
        private List<String> details;
    }

    private List<SearchResult> results;
}
