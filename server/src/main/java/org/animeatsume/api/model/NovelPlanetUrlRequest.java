package org.animeatsume.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovelPlanetUrlRequest {
    private URI novelPlanetUrl;
}
