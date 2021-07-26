package org.animeatsume.api.model.kimanime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class TitleSearchRequest {
    private String text = "";
    private String language = "Both";
    private String letter = "All";
    private String sort = "MostMatched";
    private String status = "All";
    private String type = "All";

    public TitleSearchResults(String searchText) {
        this.text = searchText;
    }
}
