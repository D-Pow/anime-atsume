package org.animeatsume.api.controller;

import org.animeatsume.api.model.TitleSearchRequest;
import org.animeatsume.api.model.himovies.HiMoviesSearchResponse;
import org.animeatsume.api.service.HiMoviesService;
import org.animeatsume.api.utils.regex.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class HiMoviesController {
    private static final Logger log = LoggerFactory.getLogger(HiMoviesController.class);

    @Autowired
    HiMoviesService hiMoviesService;

    public ResponseEntity<HiMoviesSearchResponse> searchHiMovies(TitleSearchRequest request) {
        request.setTitle(RegexUtils.removeNonAlphanumericChars(request.getTitle()));

        return hiMoviesService.searchHiMovies(request.getTitle());
    }
}
