package org.animeatsume.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.animeatsume.api.utils.ObjectUtils;
import org.gradle.internal.impldep.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class ZoroToShowResponse {
    private String html;
    private boolean status;

    public static ZoroToShowResponse fromString(String str) {
        return ObjectUtils.sanitizeAndParseJsonToClass(str, ZoroToShowResponse.class);
    }

    public static ZoroToShowResponse fromMap(Map<String, Object> likelyLinkedHashMap) {
        String html = (String) likelyLinkedHashMap.get("html");
        boolean status;

        try {
            status = (Boolean) likelyLinkedHashMap.get("status");
        } catch (Exception isStringInsteadOfBoolean) {
            status = Boolean.parseBoolean((String) likelyLinkedHashMap.get("status"));
        }

        return new ZoroToShowResponse(html, status);
    }
}
