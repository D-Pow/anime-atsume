package org.animeatsume.api.model.kimanime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class TitleSearchResults {
    private Result result;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {
        private int from;  // search results from page M
        private int to;  // search results to page N
        private int total;  // how many search results exist in total
        private int per_page;  // how many search results exist per page listing
        public static String searchResultByPageUrlSubPath = "?page=";
        private List<Data> Data;

        public static class Data {
            private int id;   // Used for actual KimAnime website serching
            private String title;   // Name of the show
            private String cover;   // Cover image for the show
            private URL mal_url;   // MyAnimeList URl for the show
            private String summary;   // Description of the show
            private String type;   // If it's a TV series, movie, etc.
        }

        public URL getShowUrl(String title) {
            return "/anime/" + this.id + "/" + this.title;
        }
    }
}
