package org.animeatsume.api.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchAnimeResponse extends TitlesAndEpisodes {
    private String error = "";
}
