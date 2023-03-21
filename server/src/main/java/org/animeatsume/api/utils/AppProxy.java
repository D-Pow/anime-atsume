package org.animeatsume.api.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class AppProxy {
    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Proxy {
        private String ip;
        private String port;
    }

    private static final String IP_TEST_URL = "https://api.ipify.org";
    private static final String RESIDENTIAL_PROXY_GEN_URL = "http://pubproxy.com/api/proxy";
    private static final Map<String, List<String>> JVM_HTTP_PROXY_OPTIONS;
    private static final List<Proxy> AVAILABLE_PROXIES = new ArrayList<>();
    private static final Set<Proxy> USED_PROXIES = new HashSet<>();

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

    private static void addUnusedProxiesToAvailableProxies(List<Proxy> proxies) {
        proxies.forEach(proxy -> {
            if (!USED_PROXIES.contains(proxy) && !AVAILABLE_PROXIES.contains(proxy)) {
                AVAILABLE_PROXIES.add(proxy);
            }
        });
    }

    private static boolean getNewResidentialProxies() {
        // PubProxy URL query param options and their optimum values
        String[][] options = new String[][] {
            { "format", "txt" },
            { "limit", "5" }, // free tier caps response list at 5
            { "type", "http" },
            { "level", "anonymous" },
            { "https", "true" },
            { "post", "true" },
            { "cookies", "true" }
        };
        String urlOptions = Arrays.stream(options)
            .map(option -> String.format("%s=%s", option[0], option[1]))
            .collect(Collectors.joining("&"));
        String proxyGeneratorUrl = RESIDENTIAL_PROXY_GEN_URL + "?" + urlOptions;
        String newProxiesIpsAndPorts = new RestTemplate()
            .getForEntity(proxyGeneratorUrl, String.class)
            .getBody();

        if (newProxiesIpsAndPorts != null && !newProxiesIpsAndPorts.isEmpty()) {
            List<Proxy> receivedProxies = Arrays.stream(newProxiesIpsAndPorts.split("\n"))
                .map(proxyEntry -> {
                    String[] proxyFields = proxyEntry.split(":");

                    return new Proxy(proxyFields[0], proxyFields[1]);
                })
                .collect(Collectors.toList());

            addUnusedProxiesToAvailableProxies(receivedProxies);

            return true;
        }

        return false;
    }

    /**
     * Gets a new residential proxy IP/port and attempts to set the
     * system-wide proxy to use it.
     *
     * If the obtained residential proxy fails to connect, then it
     * will continue to get other residential proxies until one succeeds.
     */
    public static void setHttpProxyToNewResidentialProxy() {
        boolean setProxySuccess = false;

        while (!setProxySuccess) {
            if (AVAILABLE_PROXIES.isEmpty()) {
                boolean newProxiesObtained = getNewResidentialProxies();

                if (!newProxiesObtained) {
                    log.info("New residential proxies could not be obtained.");
                    return;
                }
            }

            if (AVAILABLE_PROXIES.size() > 0) {
                Proxy newProxy = AVAILABLE_PROXIES.remove(AVAILABLE_PROXIES.size() - 1);
                USED_PROXIES.add(newProxy);

                log.info("New residential proxy obtained ({}), {} left. Setting system proxy now...", newProxy, AVAILABLE_PROXIES.size());
                setProxySuccess = setHttpProxy(newProxy.getIp(), newProxy.getPort());
            } else {
                log.info("All available proxies have been used.");
                return;
            }
        }
    }
}
