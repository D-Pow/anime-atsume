package org.animeatsume.api.service;

import lombok.extern.slf4j.Slf4j;
import org.animeatsume.api.model.TitlesEpisodesSearchResults;
import org.animeatsume.api.model.TitlesEpisodesSearchResults.TitleResults;
import org.animeatsume.api.utils.http.CorsProxy;
import org.animeatsume.api.utils.http.Requests;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FourAnimeService {
    private static final String ORIGIN = "https://4anime.to";
    private static final String SEARCH_URL = ORIGIN + "/wp-admin/admin-ajax.php";
    private static final String TITLE_ANCHOR_SELECTOR = "a.name";

    @Value("${org.animeatsume.mock-firefox-user-agent}")
    private String mockFirefoxUserAgent;

    private static String[][] getTitleSearchHttpEntity(String title) {
        return new String[][] {
            { "action", "ajaxsearchlite_search" },
            { "asid", "1" },
            { "options", "qtranslate_lang=0&set_intitle=None&customset%5B%5D=anime" },
            { "aslp", title }
        };
    }

    private HttpHeaders getNecessaryRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", mockFirefoxUserAgent);
        headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.ALL));
        return headers;
    }

    public TitlesEpisodesSearchResults searchTitle(String title) {
        String[][] titleSearchFormData = getTitleSearchHttpEntity(title);
        HttpEntity titleSearchHttpEntity = Requests.getFormDataHttpEntity(getNecessaryRequestHeaders(), titleSearchFormData);

        String searchResponseHtml = (String) CorsProxy.doCorsRequest(
            HttpMethod.POST,
            URI.create(SEARCH_URL),
            URI.create(ORIGIN),
            titleSearchHttpEntity.getBody(),
            titleSearchHttpEntity.getHeaders()
        ).getBody();

        if (searchResponseHtml != null) {
            Document titleResultsDocument = Jsoup.parse(searchResponseHtml);
            Elements titleAnchors = titleResultsDocument.select(TITLE_ANCHOR_SELECTOR);

            return new TitlesEpisodesSearchResults(titleAnchors.stream()
                .map(element -> new TitleResults(
                    element.attr("href"),
                    element.text()
                ))
                .collect(Collectors.toList()));
        }

        return null;
    }
}
