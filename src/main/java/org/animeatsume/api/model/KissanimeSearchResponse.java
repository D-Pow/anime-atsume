package org.animeatsume.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class KissanimeSearchResponse {
    @Data
    @AllArgsConstructor
    public static class SearchResponse {
        private String url;
        private String title;
    }

    private List<SearchResponse> results;
}
