package org.animeatsume.api.utils.regex;

import java.util.List;

public class HtmlParser {
    private static final String ANCHOR_URL_REGEX = "(?<=href=\")[^\"]+";
    private static final String ANCHOR_TEXT_REGEX = "(?<=\">)[^<]+";
    private static final String TITLE_REGEX = "(?<=<title>).+(?=</title>)";

    public static String getUrlFromAnchor(String anchor) {
        List<String> firstMatchingGroups = RegexUtils.getFirstMatchGroups(ANCHOR_URL_REGEX, anchor);

        return firstMatchingGroups.size() > 0 ? firstMatchingGroups.get(0) : null;
    }

    public static String getTextFromAnchor(String anchor) {
        List<String> firstMatchingGroups = RegexUtils.getFirstMatchGroups(ANCHOR_TEXT_REGEX, anchor);

        return firstMatchingGroups.size() > 0 ? firstMatchingGroups.get(0) : null;
    }

    public static String getTitleTextFromHtml(String html) {
        List<String> firstMatchingGroups = RegexUtils.getFirstMatchGroups(TITLE_REGEX, html);

        return firstMatchingGroups.size() > 0 ? firstMatchingGroups.get(0) : null;
    }
}
