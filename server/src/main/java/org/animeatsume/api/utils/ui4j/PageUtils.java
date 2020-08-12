package org.animeatsume.api.utils.ui4j;

import io.webfolder.ui4j.api.browser.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static String getHtml(Page page) {
        return (String) page.executeScript("document.documentElement.outerHTML");
    }
}
