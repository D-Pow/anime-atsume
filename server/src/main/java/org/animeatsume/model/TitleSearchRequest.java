package org.animeatsume.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class TitleSearchRequest {
    private String title;
}
