package org.animeatsume.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SearchAnimeResponse extends TitlesAndEpisodes {
    private String error = "";

    public SearchAnimeResponse(List<? extends Anchor> episodes) {
        super(episodes);
    }

    public SearchAnimeResponse(TitlesAndEpisodes episodes) {
        super(episodes.getResults());
    }
}
