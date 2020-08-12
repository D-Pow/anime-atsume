package org.animeatsume.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.HttpCookie;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CookieResponse {
    private String name;
    private String value;
    private String comment;
    private String commentURL;
    private String domain;
    private Long maxAge;
    private String path;
    private String portlist;
    private Boolean secure;
    private Boolean httpOnly;
    private Integer version;
    private Boolean discard;

    public HttpCookie toHttpCookie() {
        HttpCookie cookie = new HttpCookie(this.getName(), this.getValue());

        cookie.setComment(this.getComment());
        cookie.setCommentURL(this.getCommentURL());
        cookie.setDomain(this.getDomain());
        cookie.setMaxAge(this.getMaxAge());
        cookie.setPath(this.getPath());
        cookie.setPortlist(this.getPortlist());
        cookie.setSecure(this.getSecure());
        cookie.setHttpOnly(this.getHttpOnly());
        cookie.setVersion(this.getVersion());
        cookie.setDiscard(this.getDiscard());

        return cookie;
    }

    public static CookieResponse fromHttpCookie(HttpCookie httpCookie) {
        return new CookieResponse(
            httpCookie.getName(),
            httpCookie.getValue(),
            httpCookie.getComment(),
            httpCookie.getCommentURL(),
            httpCookie.getDomain(),
            httpCookie.getMaxAge(),
            httpCookie.getPath(),
            httpCookie.getPortlist(),
            httpCookie.getSecure(),
            httpCookie.isHttpOnly(),
            httpCookie.getVersion(),
            httpCookie.getDiscard()
        );
    }
}
