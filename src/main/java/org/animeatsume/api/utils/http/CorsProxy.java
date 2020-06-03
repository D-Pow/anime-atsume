package org.animeatsume.api.utils.http;

import org.springframework.http.HttpEntity;

public class CorsProxy {
    public static <T> HttpEntity<T> getCorsEntity(T body, String origin, String referer) {
        return CorsProxy.getCorsEntityWithCookie(body, origin, referer, null);
    }

    public static <T> HttpEntity<T> getCorsEntityWithCookie(T body, String origin, String referer, String cookie) {
        String[] originHeader = new String[] { "Origin", origin };
        String[] refererHeader = new String[] { "Referer", referer };

        String[][] corsHeaders = cookie != null
            ? new String[][] {
                originHeader,
                refererHeader,
                { "Cookie", cookie }
            }
            : new String[][] {
                originHeader,
                refererHeader
            };

        return Requests.getHttpEntityWithHeaders(body, corsHeaders);
    }
}
