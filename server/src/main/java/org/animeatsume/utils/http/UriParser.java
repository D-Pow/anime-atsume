package org.animeatsume.utils.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UriParser {
    private static final String protocolOriginSeparator = "://";

    public static String getOrigin(String uri) {
        return getOrigin(URI.create(uri));
    }

    public static String getOrigin(URI uri) {
        return uri.getScheme() + protocolOriginSeparator + uri.getHost();
    }

    public static List<String> getPathSegments(String uri) {
        return getPathSegments(URI.create(uri));
    }

    /**
     * Extracts individual path entries, separated by a {@code "/"}, from a URI.
     *
     * <p>
     * Alternative:
     * <pre>
     * RegexUtils.getAllMatchesAndGroups("(?<=/)([^/]+)(/)?$", uri.toString()).get(indexOfDesiredEntry);
     * </pre>
     *
     * @param uri - URI from which to extract path entries.
     * @return Path entries of the URI.
     */
    public static List<String> getPathSegments(URI uri) {
        List<String> pathSegments = new ArrayList<>(Arrays.asList(uri.getPath().split("/")));

        return pathSegments;
    }

    public static String getPathSegment(String uri, int i) {
        return getPathSegment(URI.create(uri), i);
    }

    public static String getPathSegment(URI uri, int i) {
        List<String> pathSegments = getPathSegments(uri);
        int pathSegmentIndex = i < 0
            ? pathSegments.size() + i
            : i >= pathSegments.size()
                ? pathSegments.size() - 1
                : i;

        return pathSegments.get(pathSegmentIndex);
    }

    public static Map<String, List<String>> getQueryParams(String uri) {
        return getQueryParams(URI.create(uri));
    }

    /**
     * Extracts query parameters from a URI.
     *
     * @param uri - URI from which to extract query parameters.
     * @return All query parameter keys and their respective (list of) values.
     *
     * @see <a href="https://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection/13592567#13592567">SO answer on query param extraction</a>
     */
    public static Map<String, List<String>> getQueryParams(URI uri) {
        // URI.getQuery() already decodes keys/values, so no decoding necessary here
        return Arrays.stream(uri.getQuery().split("&"))
            .reduce(new HashMap<>(), (map, keyValPair) -> {
                String[] keyValSplit = keyValPair.split("=");
                String key = keyValSplit[0];
                String val = keyValSplit[1];

                if (map.get(key) != null && !map.get(key).isEmpty()) {
                    map.get(key).add(val);
                } else {
                    map.put(key, Arrays.asList(val));
                }

                return map;
            }, (mapIterationA, mapIterationB) -> {
                mapIterationA.putAll(mapIterationB);

                return mapIterationA;
            });
    }
}
