package org.animeatsume.api.utils.ui4j;

import io.webfolder.ui4j.api.browser.Page;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PageUtils {

    public static String getTitle(Page page) {
        String title = page.getDocument().getTitle();

        return title != null && !title.isBlank()
            ? title
            : "";
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
