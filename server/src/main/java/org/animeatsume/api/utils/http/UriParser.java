package org.animeatsume.api.utils.http;

import java.net.URI;

public class UriParser {
    private static final String protocolOriginSeparator = "://";

    public static String getOrigin(URI uri) {
        return uri.getScheme() + protocolOriginSeparator + uri.getHost();
    }
}
