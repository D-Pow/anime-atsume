package org.animeatsume.api.model;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.animeatsume.api.utils.ObjectUtils;
import org.jsoup.Jsoup;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Data
public class ZoroToEpisodes {
    private boolean status;
    private String html;

    public static ZoroToEpisodes fromString(String json) {
        return ObjectUtils.classFromJson(json, ZoroToEpisodes.class);
    }

    public List<Anchor> getWatchAnchors() {
        return this.getWatchAnchors("");
    }
    public List<Anchor> getWatchAnchors(String origin) {
        return Jsoup.parse(this.html).select(".ssl-item.ep-item").stream()
            .map(anchor -> {
                String showUrl = origin + anchor.attr("href");
                String showTitle = anchor.attr("title");

                return new Anchor(showUrl, showTitle);
            })
            .filter(anchor ->(
                anchor.getUrl() != null
                && anchor.getUrl().length() > 0
                && anchor.getTitle() != null
                && anchor.getTitle().length() > 0
            ))
            .collect(Collectors.toList());
    }
}
