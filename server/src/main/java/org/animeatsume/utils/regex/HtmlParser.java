package org.animeatsume.utils.regex;

import java.util.List;
import java.util.regex.Pattern;

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

    public static String getIframeSrc(String html) {
        try {
            String iframeSrcRegex = "(<iframe[^>]+my_video_1[^>]+src=[\"'])([^\"']+)";
            String srcUrl = RegexUtils.getFirstMatchGroups(iframeSrcRegex, html, Pattern.CASE_INSENSITIVE).get(2);

            if (srcUrl.isEmpty()) {
                return null;
            }

            return srcUrl;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
