package org.lagalag.crawldaddy;

/**
 * Class that encapsulates parameters used by the crawler engine.
 *
 */
public class CrawldaddyParams {
    public static final int DEFAULT_MAX_INTERNAL_LINKS = 3000;
    
    private String url;
    private int maxInternalLinks = DEFAULT_MAX_INTERNAL_LINKS;

    public CrawldaddyParams() {
    }

    public CrawldaddyParams(String url) {
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMaxInternalLinks(int maxNumInternalLinks) {
        this.maxInternalLinks = maxNumInternalLinks;
    }

    public String getUrl() {
        return url;
    }

    public int getMaxInternalLinks() {
        return maxInternalLinks;
    }
}