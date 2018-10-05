package org.lagalag.crawldaddy;

/**
 * Class that encapsulates parameters used by the crawler engine.
 *
 */
public class CrawldaddyParams {
    public static final int DEFAULT_MAX_INTERNAL_LINKS = 3000;
    
    private String url;
    private int maxInternalLinks = DEFAULT_MAX_INTERNAL_LINKS;
    private boolean showVisitedLink;
    
    public CrawldaddyParams(String url, Integer maxInternalLinks, boolean showVisitedLink) {
        this.url = url;
        if (maxInternalLinks != null) {
            this.maxInternalLinks = maxInternalLinks;
        }
        this.showVisitedLink = showVisitedLink;
    }
    
    public CrawldaddyParams(String url, CrawldaddyParams paramsToCopy) {
        this.url = url;
        this.maxInternalLinks = paramsToCopy.maxInternalLinks;
        this.showVisitedLink = paramsToCopy.showVisitedLink;
    }
    
    public String getUrl() {
        return url;
    }

    public int getMaxInternalLinks() {
        return maxInternalLinks;
    }

    public boolean getShowVisitedLink() {
        return showVisitedLink;
    }
}