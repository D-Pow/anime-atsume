package org.animeatsume.api.utils.regex;

public class HtmlParser {
    private static final String ANCHOR_URL_REGEX = "(?<=href=\")[^\"]+";
    private static final String ANCHOR_TEXT_REGEX = "(?<=\">)[^<]+";

    public static String getUrlFromAnchor(String anchor) {
        return RegexUtils.getFirstMatch(ANCHOR_URL_REGEX, anchor);
    }

    public static String getTextFromAnchor(String anchor) {
        return RegexUtils.getFirstMatch(ANCHOR_TEXT_REGEX, anchor);
    }
}
