package org.animeatsume.api.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AppProxy {
    private static final String IP_TEST_URL = "https://api.ipify.org";
    private static final Map<String, List<String>> JVM_HTTP_PROXY_OPTIONS;

    static {
        JVM_HTTP_PROXY_OPTIONS = new HashMap<>();

        JVM_HTTP_PROXY_OPTIONS.put("host", Arrays.asList(
            "http.proxyHost",
            "https.proxyHost"
        ));

        JVM_HTTP_PROXY_OPTIONS.put("port", Arrays.asList(
            "http.proxyPort",
            "https.proxyPort"
        ));
    }

    /**
     * Sets the system-wide proxy to the specified {@code host} and {@code port}.
     *
     * Verifies that the proxy connected correctly and that the IP address successfully
     * changed by comparing the desired {@code host} string with the app's actual IP
     * address as determined by an API call.
     *
     * @param host - IP address of proxy.
     * @param port - Port number of proxy.
     * @return If setting the system proxy was successful.
     */
    public static boolean setHttpProxy(String host, String port) {
        try {
            JVM_HTTP_PROXY_OPTIONS.get("host").forEach(hostOption -> System.setProperty(hostOption, host));
            JVM_HTTP_PROXY_OPTIONS.get("port").forEach(portOption -> System.setProperty(portOption, port));

            log.info("Set system proxy to {}:{}", host, port);

            return getIp().equals(host);
        } catch (Exception e) {
            log.error("Unable to set proxy, will unset now. Error = {}",
                e.getMessage()
            );
            unsetHttpProxy();

            return false;
        }
    }

    public static void unsetHttpProxy() {
        JVM_HTTP_PROXY_OPTIONS.get("host").forEach(System::clearProperty);
        JVM_HTTP_PROXY_OPTIONS.get("port").forEach(System::clearProperty);

        log.info("Unset system proxy");
    }

    public static String getIp() {
        return new RestTemplate()
            .getForEntity(IP_TEST_URL, String.class)
            .getBody();
    }
}
