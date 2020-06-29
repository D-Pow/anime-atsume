import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.brotli.dec.BrotliInputStream;

/*
 * Dependencies needed:
 * implementation 'org.brotli:dec:0.1.2' // apache doesn't include Brotli decompression
 * implementation 'org.apache.commons:commons-jcs-core:2.2.1'
 * implementation 'org.apache.commons:commons-compress:1.20'
 */

public class GzipDecompressionNotes {
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
            InputStream inputStream;

            if (encoding.equals(CompressorStreamFactory.BROTLI) && false) {
                inputStream = new BrotliInputStream(bodyInputStream);
            } else {
                inputStream = new CompressorStreamFactory(true).createCompressorInputStream(encoding, bodyInputStream);
            }

            log.info("Length of input = {}", body.getBytes().length);
            IOUtils.copy(inputStream, outputStream);

            String output = new String(outputStream.toByteArray());
            log.info("Output was: {}", output);
            return output;
        } catch (CompressorException | IOException e) {
            log.error("Error decompressing response body. Error = {}", e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
