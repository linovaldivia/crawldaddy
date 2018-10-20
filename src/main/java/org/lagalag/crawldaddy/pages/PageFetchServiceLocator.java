package org.lagalag.crawldaddy.pages;

public class PageFetchServiceLocator {
    private static final PageFetchService pageFetchService = new JsoupPageFetchService();
    
    public static PageFetchService getService() {
        return pageFetchService;
    }
    
    private PageFetchServiceLocator() {
        // Prevents instantiation.
    }
}
