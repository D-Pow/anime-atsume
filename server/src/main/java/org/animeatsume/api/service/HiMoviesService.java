package org.animeatsume.api.service;

import io.webfolder.ui4j.api.browser.BrowserEngine;
import io.webfolder.ui4j.api.browser.BrowserFactory;
import io.webfolder.ui4j.api.browser.Page;
import io.webfolder.ui4j.api.browser.PageConfiguration;
import io.webfolder.ui4j.api.interceptor.Interceptor;
import io.webfolder.ui4j.api.interceptor.Request;
import io.webfolder.ui4j.api.interceptor.Response;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.animeatsume.api.model.himovies.HiMoviesSearchResponse;
import org.animeatsume.api.utils.regex.HtmlParser;
import org.animeatsume.api.utils.regex.RegexUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class HiMoviesService {
    private static final Logger log = LoggerFactory.getLogger(HiMoviesService.class);

    private static final BrowserEngine browser = BrowserFactory.getWebKit();

    private static final String HI_MOVIES_ORIGIN = "https://www2.himovies.to";
    private static final String SEARCH_URL_PREFIX = HI_MOVIES_ORIGIN + "/search/";
    private static final String IMG_TAG = "img";
    private static final String ANCHOR_TAG = "a";
    private static final String SEARCH_RESULT_PARENT_SELECTOR = ".flw-item";
    private static final String SEARCH_RESULT_TITLE_SELECTOR = ".film-name";
    private static final String SEARCH_RESULT_DETAILS_SELECTOR = ".fdi-item";
    private static final String WATCH_PLAYER_PARENT_SELECTOR = ".watching_player";
    private static final String RECAPTCHA_SITE_KEY_NAME = "recaptcha_site_key";

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

    @Async
    public void getMp4FileFromUrl(String url) {
        PageConfiguration pageConfiguration = new PageConfiguration()
            .setAlertHandler(event -> {
                log.info("Alert event was = {}", event);
            })
            .setInterceptor(new Interceptor() {
                @Override
                public void beforeLoad(Request request) {
                    log.info("Request = {}", request);
                }

                @Override
                public void afterLoad(Response response) {
                    log.info("Response = {}", response);
                }
            });
        pageConfiguration.setInterceptAllRequests(true);
        Page videoHostPage = browser.navigate(HI_MOVIES_ORIGIN, pageConfiguration);

//        try {
//            String iframeSrc;
//
//            do {
//                String iframeHtml = (String) videoHostPage.executeScript("document.getElementById('iframe-embed').outerHTML");
//                iframeSrc = HtmlParser.getIframeSrc(iframeHtml);
//
//                log.info("iframe HTML = {}", iframeHtml);
//
//                Thread.sleep(3000);
//            } while (iframeSrc == null);
//
//            log.info("iframe src = {}", iframeSrc);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


//        videoHostPage.show();
        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        List<HttpCookie> cookies = cookieManager.getCookieStore().get(URI.create("https://www.google.com"));
        log.info("Google cookies = {}", cookies);

log.info("Executing script now");
//        videoHostPage.executeScript("grecaptcha.execute(recaptcha_site_key, { action: 'get_link'}).then(function(token) {" +
//            "window.token = token;" +
//            "alert(token);" +
//            "})"
//        );
        videoHostPage.executeScript("/* PLEASE DO NOT COPY AND PASTE THIS CODE. */(function(){var w=window,C='___grecaptcha_cfg',cfg=w[C]=w[C]||{},N='grecaptcha';var gr=w[N]=w[N]||{};gr.ready=gr.ready||function(f){(cfg['fns']=cfg['fns']||[]).push(f);};(cfg['render']=cfg['render']||[]).push('6LfHPLoUAAAAAO0Jylr8Bn5RptHLGDdGuDybODPA');w['__google_recaptcha_client']=true;var d=document,po=d.createElement('script');po.type='text/javascript';po.async=true;po.src='https://www.gstatic.com/recaptcha/releases/nuX0GNR875hMLA1LR7ayD9tc/recaptcha__en.js';var e=d.querySelector('script[nonce]'),n=e&&(e['nonce']||e.getAttribute('nonce'));if(n){po.setAttribute('nonce',n);}var s=d.getElementsByTagName('script')[0];s.parentNode.insertBefore(po, s);})();");

//        videoHostPage.executeScript("fetch(window.location.href).then(alert)");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        videoHostPage.executeScript("grecaptcha.execute(recaptcha_site_key, { action: 'get_link' }).then(alert).catch(alert)");
        String token = (String) videoHostPage.executeScript("window.token");
        log.info("Token = {}", token);
//        log.info("Async = {}", videoHostPage.executeScript("(fetch).toString()"));

//        CountDownLatch waitForJsobjectLatch = new CountDownLatch(1);
//        AtomicReference<JSObject> obj = new AtomicReference<>();
//        Platform.runLater(() -> {
//            WebEngine engine = (WebEngine) videoHostPage.getEngine();
//            engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
//                log.info("o ({}), old ({}), new ({})", observable, oldValue, newValue);
//            });
//            JSObject jsobject = (JSObject) videoHostPage.executeScript("grecaptcha.execute(recaptcha_site_key, { action: 'get_link'}).then(function(token) {" +
//                "window.token = token;" +
//                "})"
//            );
//            obj.set(jsobject);
//            log.info("Execute script = {}", jsobject);
//
//            String token = "undefined";
//
//            try {
//                do {
//                    token = (String) videoHostPage.executeScript("window.token");
//
//                    log.info("Token (probably null) = {}", token);
//
//                    Thread.sleep(3000);
//                } while (token.equals("undefined"));
//
//                log.info("Token (obtained) = {}", token);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
////            waitForJsobjectLatch.countDown();
//        });
//
//        try {
//            waitForJsobjectLatch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//            Map<String, Object> obj = new WebKitMapper(videoHostPage).toJava();
    }

    public void getMp4FileFromUrl_jsoup(String url) {
        try {
            Document videoHostDocument = Jsoup.connect(url).get();
            String recaptchaSiteKeyRegex = "(" + RECAPTCHA_SITE_KEY_NAME + " ?= ? ['\"])([^'\"]+)"; // JS variable: site_key = 'keyvalue';
            String recaptchaSiteKey = RegexUtils.getFirstMatchGroups(recaptchaSiteKeyRegex, videoHostDocument.html()).get(2);

            log.info("Site key = {}", recaptchaSiteKey);
        } catch (IOException e) {
            log.error("Error fetching HiMovies video host document. Error: {}", e.getMessage());
        }
    }
}
