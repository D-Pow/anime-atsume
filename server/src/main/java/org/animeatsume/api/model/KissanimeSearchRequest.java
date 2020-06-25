package org.animeatsume.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class KissanimeSearchRequest {
    private String title;
}
