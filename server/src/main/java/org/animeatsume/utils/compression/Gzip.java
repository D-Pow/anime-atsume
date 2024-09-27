package org.animeatsume.utils.compression;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.IOUtils;
/*
 * Dependencies needed:
 * implementation 'org.brotli:dec:0.1.2'  // apache doesn't include Brotli decompression
 * implementation 'org.apache.commons:commons-jcs-core:2.2.1'
 * implementation 'org.apache.commons:commons-compress:1.20'
 *
 * Since this file isn't currently used, just default to Gradle's nested dependencies.
 */
import org.gradle.internal.impldep.org.apache.commons.compress.compressors.CompressorException;
import org.gradle.internal.impldep.org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;


@Log4j2
public class Gzip {
    public static String decodeGzipContent(String encoding, String body) {
        /*
         * This method is incomplete and not working, but some progress was
         * made in trying different approaches.
         *
         * Relevant links:
         * https://stackoverflow.com/a/34415146
         * https://stackoverflow.com/questions/34415144/how-to-parse-gzip-encoded-response-with-resttemplate-in-spring-web/34415146#34415146
         * https://stackoverflow.com/questions/16638345/how-to-decode-gzip-compressed-request-body-in-spring-mvc
         * https://stackoverflow.com/questions/20507007/http-request-compression#answer-20576628
         * https://stackoverflow.com/questions/46596899/why-is-gzip-compression-of-a-request-body-during-a-post-method-uncommon
         * https://commons.apache.org/proper/commons-compress/examples.html
         *
         * Originally, the code in the comments below were applied before
         * calling this function:
         *
         * byte[] bodyBytes = ((String) body).getBytes(StandardCharsets.UTF_8);
         * ByteArrayInputStream inputStream = new ByteArrayInputStream(bodyBytes);
         * //  String decodedGzipContent = new String(CompressionUtil.decompressByteArray(bodyBytes));
         * String contentEncodingHeader = response.getHeaders().get(HttpHeaders.CONTENT_ENCODING).get(0);
         * String decodedGzipContent = decodeGzipContent(contentEncodingHeader, (String) body);
         * log.info("Input stream ({}), body ({}), Decoded gzip content = {}", inputStream, body, decodedGzipContent);
         * //  body = parseJsonStringToObject(decodedGzipContent, responseType);
         */
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayInputStream bodyInputStream = new ByteArrayInputStream(body.getBytes());
            InputStream inputStream = new CompressorStreamFactory(true)
                .createCompressorInputStream(encoding, bodyInputStream);

            if (encoding.equals(CompressorStreamFactory.BROTLI)) {
                // TODO - Is this required?
                //inputStream = new BrotliInputStream(bodyInputStream);
            }

            log.info("Length of input = {}", body.getBytes().length);

            IOUtils.copy(new InputStreamReader(inputStream), new OutputStreamWriter(outputStream));
            String output = outputStream.toString(StandardCharsets.UTF_8);

            log.info("Output was: {}", output);

            return output;
        } catch (CompressorException | IOException e) {
            log.error("Error decompressing response body. Error = {}", e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
