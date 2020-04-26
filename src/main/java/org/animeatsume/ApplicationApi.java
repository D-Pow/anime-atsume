package org.animeatsume;

import org.animeatsume.api.controller.NovelPlanetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApplicationApi {
    @Autowired
    NovelPlanetController novelPlanetController;

//    @CrossOrigin
//    @RequestMapping("/greeting")
//    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
//        return greetingController.greeting(name);
//    }

//    @PostMapping("/greeting")
//    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name, @RequestBody String postBody) {
//        return greetingController.greeting(name, postBody);
//    }
}
