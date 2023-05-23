package org.animeatsume.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.animeatsume.api.utils.ObjectUtils;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
