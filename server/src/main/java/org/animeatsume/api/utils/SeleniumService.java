package org.animeatsume.api.utils;

import com.sun.webkit.network.URLs;
import com.sun.webkit.network.about.Handler;
import io.webfolder.ui4j.api.browser.BrowserEngine;
import io.webfolder.ui4j.api.browser.BrowserFactory;
import io.webfolder.ui4j.api.browser.BrowserType;
import io.webfolder.ui4j.api.browser.Page;
import io.webfolder.ui4j.api.browser.PageConfiguration;
import io.webfolder.ui4j.api.dom.Element;
import io.webfolder.ui4j.api.util.Ui4jException;
import io.webfolder.ui4j.webkit.WebKitBrowserProvider;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;
import org.animeatsume.api.utils.ui4j.PageUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Log4j2
//@Component
@Service
public class SeleniumService {
    private static final String CLOUDFLARE_TITLE = "Just a moment";

    private static final String BANNED_TITLE = "Access Denied";

    // BrowserEngine is a singleton; improve performance by avoiding making `synchronized` calls in endpoint handling
//    private static BrowserEngine browser = BrowserFactory.getWebKit();

    private static PageConfiguration pageConfiguration;

    private BrowserEngine browser;
//    private BrowserEngine browser = BrowserFactory.getBrowser(BrowserType.WebKit);

    @Value("${server.port}")
    private int port;

    @Value("${org.animeatsume.mock-firefox-user-agent}")
    private String userAgent;

    @Value("${org.animeatsume.num-attempts-to-bypass-cloudflare}")
    private int numAttemptsToBypassCloudflare;

//    public SeleniumService() {
////        this.patch();
//        this.setup();
//    }

//    @Autowired
    public SeleniumService(
        @Value("${org.animeatsume.mock-firefox-user-agent}") String mockUserAgent,
        @Value("${org.animeatsume.num-attempts-to-bypass-cloudflare}") Integer numAttemptsToBypassCloudflare,
        @Value("${server.port}") int port
    ) {
//        this();

        this.userAgent = mockUserAgent;
        this.numAttemptsToBypassCloudflare = numAttemptsToBypassCloudflare;
        this.port = port;

        this.setup();
//        browser = BrowserFactory.getWebKit();

        log.info("SeleniumService INIT, {}", this.browser);
    }

    public void setup() {
        log.info("CALLING this.setup()!!!");
//        log.info("com.sun.webkit.network.URLs.class.getDeclaredField(\"handlerMap\"): {}", com.sun.webkit.network.URLs.class.getDeclaredField("handlerMap"));
//        browser = BrowserFactory.getBrowser(BrowserType.WebKit);
//        this.browser = BrowserFactory.getBrowser(BrowserType.JxBrowser);
//        browser = BrowserFactory.getJxBrowser();
        browser = BrowserFactory.getWebKit();
        log.info("INSTANTIATED browser!!!");

        // Setup cookie jar so browser can retain/reuse cookies
        CookieHandler.setDefault(new CookieManager());

        PageConfiguration pageConfig = new PageConfiguration();
        pageConfig.setUserAgent(this.userAgent);

        pageConfiguration = pageConfig;

//        enableGetMp4FromM3u8File();
    }

    public Har enableGetMp4FromM3u8File(String url) {
        BrowserMobProxy proxy = new BrowserMobProxyServer();

        proxy.start(this.port + 1);

        ClientUtil.createSeleniumProxy(proxy);

        proxy.enableHarCaptureTypes(
            CaptureType.REQUEST_HEADERS,
            CaptureType.REQUEST_COOKIES,
            CaptureType.REQUEST_CONTENT,
            CaptureType.REQUEST_BINARY_CONTENT,
            CaptureType.RESPONSE_HEADERS,
            CaptureType.RESPONSE_COOKIES,
            CaptureType.RESPONSE_CONTENT,
            CaptureType.RESPONSE_BINARY_CONTENT
        );

        // Create HTTP Archive (HAR) file for http tracing.
        // Script will attempt to capture all m3u8 requests produced from website loading.
        Har proxyHar = proxy.newHar(url == null ? "" : url);

        Har har = proxy.getHar();

        log.info("proxyHar: {}\nhar: {}", proxyHar, har);

        return har;
    }

    public Har getMp4FromM3u8File(String url) {
        return enableGetMp4FromM3u8File(url);
    }

    private void patch() {
//        WebDriver x = new ChromeDriver()

//        Class<?> URLsClass = Class.forName("com.sun.webkit.network.URLs");
//        URLsClass.getDeclaredField("handlerMap").setAccessible(true);
//        URLsClass.

//        patchUi4jForJavaVersionsAbove8();
        try {
            Class<?> WebKitBrowserClass = Class.forName("io.webfolder.ui4j.webkit.WebKitBrowser");
            Method applyURLsHack = WebKitBrowserClass.getDeclaredMethod("applyURLsHack");
//            Method applyURLsHack = WebKitBrowserClass.getMethod("applyURLsHack");
            applyURLsHack.setAccessible(true);
            Field field = WebKitBrowserClass.getField("applyURLsHack");
            field.setAccessible(true);
            log.info("FIELD: {}", field);
            SeleniumService.class.getField("patchUi4jForJavaVersionsAbove8").setAccessible(true);
            field.set(field, SeleniumService.class.getMethod("patchUi4jForJavaVersionsAbove8"));
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
//        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static void patchUi4jForJavaVersionsAbove8() {
        // Orig: applyURLsHack()
        try {
            ConcurrentHashMap<Object, Object> handlers = new ConcurrentHashMap();
            handlers.put("about", new Handler());
            handlers.put("data", new com.sun.webkit.network.data.Handler());
            // Orig: setFinalStatic(URLs.class.getDeclaredField("handlerMap"), handlers);
            Field field = URLs.class.getDeclaredField("HANDLER_MAP"); // was "handlerMap"
            Object newValue = handlers;
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & -17);
            field.set(null, newValue);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException var2) {
            throw new Ui4jException(var2);
        }
    }

    /**
     * Kissanime has a Cloudflare check when you first arrive on the page for the day
     * to ensure you're not a bot.
     * This check basically just waits 4 seconds and then sets a special identification
     * cookie.
     * Browsing to the page and waiting for it to load all this gives our browser the
     * cookie it needs.
     */
    @Async
    public CompletableFuture<Boolean> bypassCloudflareDdosScreen(String hostUrl, String hostTitleOfLoadedPage, boolean throwErrorOnFailure) {
        Page hostPage = navigateTo(hostUrl);

        for (int attempt = 0; attempt < this.getNumAttemptsToBypassCloudflare(); attempt++) {
            String pageTitle = PageUtils.getTitle(hostPage);

            if (!pageTitle.contains(CLOUDFLARE_TITLE) && pageTitle.contains(hostTitleOfLoadedPage)) {
                log.info("Cloudflare has been bypassed!");

                hostPage.close();

                return CompletableFuture.completedFuture(true);
            }

            if (
                pageTitle.toLowerCase().contains(BANNED_TITLE.toLowerCase())
                || PageUtils.getUrl(hostPage).matches("([Bb]anned)|" + "BANNED")
            ) {
                log.info("Redirected to banned page with title ({}) and URL ({}). Attempting circumventing by setting new proxy and re-navigating to {}",
                    pageTitle,
                    PageUtils.getUrl(hostPage),
                    hostUrl
                );

                hostPage.close();
                clearAllCookies();
                AppProxy.setHttpProxyToNewResidentialProxy();
                hostPage = navigateTo(hostUrl);
            }

            log.info("Cloudflare was not bypassed (title = \"{}\"), trying attempt {}/{}", pageTitle, attempt+1, numAttemptsToBypassCloudflare);

            try {
                // Cloudflare has 4000 ms countdown before redirecting.
                // Add an extra second to ensure it's actually redirected.
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("Error while waiting for initial page load in initialization: {}", e.getMessage());
            }
        }

        log.error("Could not bypass Cloudflare. Stuck on URL ({}) with title ({}) and body text ({})",
            PageUtils.getUrl(hostPage),
            PageUtils.getTitle(hostPage),
            PageUtils.getInnerText(hostPage)
        );

        if (throwErrorOnFailure) {
            throw new RuntimeException("Cannot bypass Cloudflare :(");
        }

        return CompletableFuture.completedFuture(false);
    }

    public void clearAllCookies() {
        CookieStore cookieStore = ((CookieManager) CookieHandler.getDefault()).getCookieStore();
        cookieStore.removeAll();

        log.info("All cookies cleared.");
    }

    public HttpCookie getAuthCookie(String hostUrl, String cookieAuthName) {
        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        List<HttpCookie> websiteCookies = cookieManager.getCookieStore().get(URI.create(hostUrl));

        HttpCookie placeholderCookie = new HttpCookie(cookieAuthName, "");
        placeholderCookie.setMaxAge(0);

        HttpCookie authCookie = websiteCookies.stream()
            .filter(cookieKeyVal -> cookieKeyVal.getName().equals(cookieAuthName))
            .findFirst()
            .orElse(placeholderCookie);

        return authCookie;
    }

    public void waitForCloudflareToAllowAccess(String hostUrl, String hostTitleOfLoadedPage, String cookieAuthName) {
        waitForCloudflareToAllowAccess(hostUrl, hostTitleOfLoadedPage, cookieAuthName, false);
    }

    /*
     * User-Agent header and cf_clearance cookie matter.
     * Origin, etc. doesn't
     */
    public void waitForCloudflareToAllowAccess(String hostUrl, String hostTitleOfLoadedPage, String cookieAuthName, boolean forceRefresh) {
        if (getAuthCookie(hostUrl, cookieAuthName).hasExpired() || forceRefresh) {
            log.info("Cookie has expired. Refreshing now...");

            try {
                bypassCloudflareDdosScreen(hostUrl, hostTitleOfLoadedPage, false).get();
            } catch (Exception e) {
                log.error("Refreshing website auth token has failed. Error: {}",
                    e.getMessage()
                );

                throw new RuntimeException("Cannot bypass Cloudflare :(");
            }
        }
    }

    public Page navigateTo(String url) {
        this.getMp4FromM3u8File(url);

        return browser.navigate(url, pageConfiguration);
    }

    public Element clickOn(String url, String querySelectorAll) {
        return this.clickOn(url, querySelectorAll, 0).get(0);
    }

    public List<Element> clickOn(String url, String querySelectorAll, int index) {
        List<Element> elements = navigateTo(url).getDocument().queryAll(querySelectorAll);

        if (index >= 0) {
            return new ArrayList<>(Arrays.asList(elements.get(index).click()));
        }

        return elements.stream()
            .map(Element::click)
            .toList();
    }
}
