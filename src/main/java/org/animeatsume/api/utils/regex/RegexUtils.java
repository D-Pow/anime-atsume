package org.animeatsume.api.utils.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    public static List<String> findAllMatches(String regex, String toSearch) {
        Pattern regexPattern = Pattern.compile(regex);
        Matcher strToSearchMatcher = regexPattern.matcher(toSearch);
        List<String> allMatches = new ArrayList<>();

        while(strToSearchMatcher.find()) {
            allMatches.add(strToSearchMatcher.group());
        }

        return allMatches;
    }

    public static String getFirstMatch(String regex, String toSearch) {
        List<String> allMatches = findAllMatches(regex, toSearch);

        return allMatches.size() > 0 ? allMatches.get(0) : null;
    }
}
