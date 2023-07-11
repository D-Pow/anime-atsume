package org.animeatsume.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VideoSearchResult extends Anchor {
    private boolean directSource = false;
    private boolean iframeSource = false;

    public VideoSearchResult(String url, String title) {
        super(url, title);
    }

    public VideoSearchResult(String url, String title, boolean directSource) {
        super(url, title);
        this.directSource = directSource;
    }

    public VideoSearchResult(String url, String title, boolean directSource, boolean iframeSource) {
        super(url, title);
        this.directSource = directSource;
        this.iframeSource = iframeSource;
    }
}
