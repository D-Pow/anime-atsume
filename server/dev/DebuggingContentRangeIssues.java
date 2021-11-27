// 199819264-201121204/201121205
// 201121204 - 199819264 = 1,301,940
// contentLength = 1301941

            log.info("ASDF final range header = {}", proxiedVideoStream.getHeaders().getValuesAsList("Content-Range"));

            try {
                Resource resource = proxiedVideoStream.getBody();
                long contentLength = resource.contentLength();
                List<HttpRange> httpRanges = requestHeaders.getRange();
                log.info("ASDF content length [{}], request RANGE [{}]", contentLength, httpRanges);
                List<ResourceRegion> resourceRegions = HttpRange.toResourceRegions(httpRanges, resource);

                log.info("Resource regions: {}", resourceRegions);
            } catch (Exception e) {
                log.error("Error converting to resource region:", e);
            }

            return proxiedVideoStream;