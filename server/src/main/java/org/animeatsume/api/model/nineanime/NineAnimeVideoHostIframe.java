package org.animeatsume.api.model.nineanime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NineAnimeVideoHostIframe {
    private String target;
    private String grabber;
    private String type;

    public String getIframeSrcUrl() {
        if (this.target != null) {
            return this.target;
        }

        return "";
    }
}
