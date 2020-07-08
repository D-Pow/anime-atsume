package org.animeatsume.cookiegetter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.HttpCookie;

@RestController
public class Api {
    @Autowired
    CookieGetter cookieGetter;

    @GetMapping("/kissanimeCookie")
    @CrossOrigin
    public ResponseEntity<HttpCookie> getKissanimeCookie() {
        cookieGetter.waitForCloudflareToAllowAccessToKissanime();
        HttpCookie authCookie = cookieGetter.getAuthCookie();

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(authCookie);
    }
}
