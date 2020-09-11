package org.animeatsume.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VideoSearchResult extends Anchor {
    private boolean directSource = false;

    public VideoSearchResult(String url, String title) {
        super(url, title);
    }

    public VideoSearchResult(String url, String title, boolean directSource) {
        super(url, title);
        this.directSource = directSource;
    }
}
