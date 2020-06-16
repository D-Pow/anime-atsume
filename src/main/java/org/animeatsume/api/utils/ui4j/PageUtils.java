package org.animeatsume.api.utils.ui4j;

import io.webfolder.ui4j.api.browser.Page;
import io.webfolder.ui4j.webkit.WebKitMapper;
import javafx.application.Platform;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PageUtils {
    private static final Logger log = LoggerFactory.getLogger(PageUtils.class);

    public static String getTitle(Page page) {
        return page.getDocument().getTitle().orElse("");
    }

    public static String getInnerText(Page page) {
        return (String) page.executeScript("document.body.innerText");
    }

    public static String getUrl(Page page) {
        return page.getWindow().getLocation();
    }

    // TODO - After making App async, activate this function
    //  WebKitMapper runs on separate thread so can't be handled on main endpoint-processing thread
    private static void getCookies(Page page) {
        String getCookieJsFunction = "function getCookie(cookie = document.cookie) {" +
            "    return cookie.split('; ').reduce((cookieObj, entry) => {" +
            "        const keyVal = entry.split('=');" +
            "        const key = keyVal[0];" +
            "        const value = keyVal.slice(1).join('=');" +
            "        cookieObj[key] = value;" +
            "        return cookieObj;" +
            "    }, {});" +
            "}";
        page.executeScript(getCookieJsFunction);
        page.executeScript("var cookiesObject = getCookie()");
//        JSObject cookiesArrayOfKeys = (JSObject) page.executeScript("Object.keys(cookiesObject)");

//        Platform.runLater(() -> {
//            // int cookiesObjectNumKeys = (int) cookiesObjectKeys.getMember("length");
//            Map<String, Object> cookies = new WebKitMapper(page).toJava((JSObject) page.executeScript("cookiesObject"));
//            log.info("Platform later cookies = {}", cookies);
//        });
//        String stringifiedCookies = (String) page.executeScript("JSON.stringify(cookies)");

        log.info("Cookie = {}", "Not supported yet");
    }

    // another alternative: CookieHandler
//        try {
//            log.info("Cookies include: {}", cookieHandler.get(URI.create(kissanimeOrigin), null));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
}
