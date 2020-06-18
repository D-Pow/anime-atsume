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
     *
     * @param regex Regex to apply to the {@code toSearch} string.
     * @param toSearch String to search for matches.
     * @return List of lists; top-level list contains separate matches,
     *          whereas each nested list contains match groups for a given match.
     */
    public static List<List<String>> getAllMatchesAndGroups(String regex, String toSearch) {
        Pattern regexPattern = Pattern.compile(regex);
        Matcher strToSearchMatcher = regexPattern.matcher(toSearch);
        List<List<String>> allMatches = new ArrayList<>();

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
     * Gets a list of all match groups for the first match find in {@code toSearch}
     * given the respective {@code regex}.
     *
     * @param regex Regex to apply to the {@code toSearch} string.
     * @param toSearch String to search for matches.
     * @return List containing all match groups for the given {@code toSearch} string.
     */
    public static List<String> getFirstMatchGroups(String regex, String toSearch) {
        List<List<String>> allMatches = getAllMatchesAndGroups(regex, toSearch);

        return allMatches.size() > 0 ? allMatches.get(0) : new ArrayList<>();
    }
}
