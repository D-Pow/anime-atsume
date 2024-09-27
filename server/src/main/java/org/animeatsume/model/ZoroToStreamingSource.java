package org.animeatsume.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.animeatsume.utils.ObjectUtils;

@Log4j2
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoroToStreamingSource {
    private String link;  // URL of streaming host
    private String type;  // Usually "iframe"

    public static ZoroToStreamingSource fromString(String json) {
        return ObjectUtils.classFromJson(json, ZoroToStreamingSource.class);
    }
}
