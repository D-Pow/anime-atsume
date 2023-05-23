package org.animeatsume.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.utils.ObjectUtils;
import org.jsoup.Jsoup;

import java.util.List;
import java.util.stream.Collectors;

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
