package org.animeatsume;

import org.animeatsume.api.controller.NovelPlanetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApplicationApi {
    @Autowired
    NovelPlanetController novelPlanetController;

//    @CrossOrigin
//    @PostMapping("/novelPlanet")
//    public void getNovelPlanetMp4Urls(@RequestBody String novelPlanetUrl, final HttpServletRequest request) {
        // find out how to get request IP address here
//    }
}
