package org.lagalag.crawldaddy.pages;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains the results of a page fetch/parse operation. Immutable.
 *
 */
public class PageFetchResults {
    private final String url;
    private final int httpStatusCode;
    private final List<String> linkUrls;
    private final List<String> scriptUrls;

    public static PageFetchResults onHttpOK(String url, List<String> links, List<String> scripts) {
        return new PageFetchResults(url, HttpURLConnection.HTTP_OK, links, scripts);
    }
    
    public static PageFetchResults onHttpNotOK(String url, int httpStatusCode) {
        return new PageFetchResults(url, httpStatusCode, Collections.emptyList(), Collections.emptyList());
    }
    
    private PageFetchResults(String url, int httpStatusCode, List<String> linkUrls, List<String> scriptUrls) {
        this.url = url;
        this.httpStatusCode = httpStatusCode;
        this.linkUrls = new ArrayList<>(linkUrls);
        this.scriptUrls = new ArrayList<>(scriptUrls);
    }
    
    public String getUrl() {
        return url;
    }
    
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
    
    public boolean isHttpStatusOK() {
        return (httpStatusCode == HttpURLConnection.HTTP_OK);
    }

    public List<String> getLinkUrls() {
        return Collections.unmodifiableList(linkUrls);
    }

    public List<String> getScriptUrls() {
        return Collections.unmodifiableList(scriptUrls);
    }
}
