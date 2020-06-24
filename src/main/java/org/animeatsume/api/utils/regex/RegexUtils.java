package org.animeatsume.api.utils.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    /**
     * Searches a given string, {@code toSearch}, with the respective {@code regex}.
     * Returns a list of lists, where each nested list contains all match groups of
     * the {@code regex}.
     * Allows selection of Pattern.SOME_OPTION.
     *
     * @param regex Regex to apply to the {@code toSearch} string.
     * @param toSearch String to search for matches.
     * @param patternOption Option from static Pattern config variable.
     * @return List of lists; top-level list contains separate matches,
     *          whereas each nested list contains match groups for a given match.
     */
    public static List<List<String>> getAllMatchesAndGroups(String regex, String toSearch, int patternOption) {
        List<List<String>> allMatches = new ArrayList<>();

        if (regex == null || toSearch == null) {
            return allMatches;
        }

        Pattern regexPattern = Pattern.compile(regex, patternOption);
        Matcher strToSearchMatcher = regexPattern.matcher(toSearch);

        while(strToSearchMatcher.find()) {
            List<String> matchGroups = new ArrayList<>();

            for (int i = 0; i <= strToSearchMatcher.groupCount(); i++) {
                matchGroups.add(strToSearchMatcher.group(i));
            }

            allMatches.add(matchGroups);
        }

        return allMatches;
    }

    /**
     * Searches a given string, {@code toSearch}, with the respective {@code regex}.
     * Returns a list of lists, where each nested list contains all match groups of
     * the {@code regex}.
     *
     * @param regex Regex to apply to the {@code toSearch} string.
     * @param toSearch String to search for matches.
     * @return List of lists; top-level list contains separate matches,
     *          whereas each nested list contains match groups for a given match.
     */
    public static List<List<String>> getAllMatchesAndGroups(String regex, String toSearch) {
        return getAllMatchesAndGroups(regex, toSearch, 0);
    }

    /**
     * Gets a list of all match groups for the first match find in {@code toSearch}
     * given the respective {@code regex}.
     *
     * @param regex Regex to apply to the {@code toSearch} string.
     * @param toSearch String to search for matches.
     * @param patternOption Option from static Pattern config variable.
     * @return List containing all match groups for the given {@code toSearch} string.
     */
    public static List<String> getFirstMatchGroups(String regex, String toSearch, int patternOption) {
        List<List<String>> allMatches = getAllMatchesAndGroups(regex, toSearch, patternOption);

        return allMatches.size() > 0 ? allMatches.get(0) : new ArrayList<>();
    }

    public static List<String> getFirstMatchGroups(String regex, String toSearch) {
        return getFirstMatchGroups(regex, toSearch, 0);
    }

    /**
     * Removes leading and trailing spaces from a string.
     *
     * @param str String from which to strip spaces.
     * @return String without leading or trailing spaces.
     */
    public static String strip(String str) {
        return str.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
    }
}
