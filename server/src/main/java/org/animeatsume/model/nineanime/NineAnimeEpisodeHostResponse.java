package org.animeatsume.model.nineanime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.animeatsume.utils.ObjectUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NineAnimeEpisodeHostResponse {
    private String grabber;
    private String target;

    public static NineAnimeEpisodeHostResponse fromString(String json) {
        return ObjectUtils.classFromJson(json, NineAnimeEpisodeHostResponse.class);
    }
}
