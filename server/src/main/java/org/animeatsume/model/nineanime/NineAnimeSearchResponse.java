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
public class NineAnimeSearchResponse {
    private String html;

    public static NineAnimeSearchResponse fromString(String json) {
        return ObjectUtils.classFromJson(json, NineAnimeSearchResponse.class);
    }
}
