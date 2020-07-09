package org.animeatsume.api.service;

import org.animeatsume.api.model.himovies.HiMoviesSearchResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HiMoviesService {
    private static final Logger log = LoggerFactory.getLogger(HiMoviesService.class);

    private static final String HI_MOVIES_ORIGIN = "https://www2.himovies.to";
    private static final String SEARCH_URL_PREFIX = HI_MOVIES_ORIGIN + "/search/";
    private static final String IMG_TAG = "img";
    private static final String ANCHOR_TAG = "a";
    private static final String SEARCH_RESULT_PARENT_SELECTOR = ".flw-item";
    private static final String SEARCH_RESULT_TITLE_SELECTOR = ".film-name";
    private static final String SEARCH_RESULT_DETAILS_SELECTOR = ".fdi-item";

    public ResponseEntity<HiMoviesSearchResponse> searchHiMovies(String searchText) {
        HiMoviesSearchResponse response = new HiMoviesSearchResponse(new ArrayList<>());

        try {
            Document searchResponseHtml = Jsoup.connect(SEARCH_URL_PREFIX + searchText.replaceAll(" ", "-")).get();
            Elements resultsParents = searchResponseHtml.select(SEARCH_RESULT_PARENT_SELECTOR);

            resultsParents.forEach(element -> {
                HiMoviesSearchResponse.SearchResult searchResult = new HiMoviesSearchResponse.SearchResult();

                element.select(SEARCH_RESULT_TITLE_SELECTOR).stream()
                    .findFirst()
                    .ifPresent(nameElement -> {
                        searchResult.setTitle(nameElement.text());
                    });
                element.getElementsByTag(IMG_TAG).stream()
                    .findFirst()
                    .ifPresent(imgElement -> {
                        String imgSrc = imgElement.attr("src");

                        if (imgSrc.isEmpty()) {
                            imgSrc = imgElement.attr("data-src");
                        }

                        searchResult.setImgSrc(imgSrc);
                    });
                element.getElementsByTag(ANCHOR_TAG).stream()
                    .findFirst()
                    .ifPresent(anchorElement -> {
                        searchResult.setShowUrl(HI_MOVIES_ORIGIN + anchorElement.attr("href"));
                    });
                List<String> details = element.select(SEARCH_RESULT_DETAILS_SELECTOR).stream()
                    .map(Element::text)
                    .collect(Collectors.toList());
                searchResult.setDetails(details);

                response.getResults().add(searchResult);
            });
        } catch (IOException e) {
            log.error("Error fetching HiMovies search document. Error: {}", e.getMessage());
            return null;
        }

        return ResponseEntity
            .ok(response);
    }
}
