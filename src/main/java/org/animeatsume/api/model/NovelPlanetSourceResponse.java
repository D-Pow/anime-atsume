package org.animeatsume.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NovelPlanetSourceResponse {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NovelPlanetSource {
        String file;
        String label;
        String type;
    }

    List<NovelPlanetSource> data;
}
