package org.animeatsume.api.model.kimanime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class ShowSearchRequest {
    private boolean episode_count = true;
    private int limit = 0;
}
