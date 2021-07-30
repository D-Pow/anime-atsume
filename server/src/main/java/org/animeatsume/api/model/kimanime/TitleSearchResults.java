package org.animeatsume.api.model.kimanime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class TitleSearchResults {
    private Result result;
    private String message;
    private String status;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties
    public static class Result {
        private int from;  // search results from page M
        private int to;  // search results to page N
        private int total;  // how many search results exist in total
        private int per_page;  // how many search results exist per page listing
        public static String searchResultByPageUrlSubPath = "?page=";
        private String first_page_url;  // URL of the first page of results (from `total` and `per_page`)
        private String next_page_url;  // URL of the next page of results (from `total` and `per_page`)
        private String last_page_url;  // URL of the last page of results (from `total` and `per_page`)
        private List<ShowInfo> data;  // Show search results

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties
        public static class ShowInfo {
            private int id;   // Used for actual KimAnime website searching
            private String title;   // Name of the show
            private String slug;   // Search query for the show
            private String cover;   // Cover image URL path
            private URI mal_url;   // MyAnimeList URL
            private String summary;   // Description
            private String type;   // If it's a TV series, movie, etc.

            public String getShowUrl() {
                return MessageFormat.format("/anime/{0}/{1}", this.id, this.title);
            }
        }
    }
}
