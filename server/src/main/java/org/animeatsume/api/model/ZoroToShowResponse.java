package org.animeatsume.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gradle.internal.impldep.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class ZoroToShowResponse {
    private String html;
    private boolean status;

    public static ZoroToShowResponse fromString(String str) {
        return new ZoroToShowResponse(str, true);
    }
}
