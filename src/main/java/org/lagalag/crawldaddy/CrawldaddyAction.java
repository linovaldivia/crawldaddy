package org.lagalag.crawldaddy;

import java.io.IOException;
import java.util.concurrent.RecursiveAction;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawldaddyAction extends RecursiveAction {
    private static final long serialVersionUID = 1L;
    
    private String url;
    private CrawldaddyResult result;

    public CrawldaddyAction(String url) {
        this.url = url;
    }
    
    /* Used only when following links from a page that has been downloaded */
    private CrawldaddyAction(String url, CrawldaddyResult result) {
        this.url = url;
        this.result = result;
    }
    
    public CrawldaddyResult getResult() {
        return this.result;
    }
    
    @Override
    protected void compute() {
        if (url != null) {
            try {
                Document doc = Jsoup.connect(url).get();
                
                if (result == null) {
                    result = new CrawldaddyResult(url);
                }
                Elements ahrefs = doc.select("a[href]");
                for (Element e : ahrefs) {
                    // TODO increment only for each link that has been visited (even attempted visits to broken links)
                    result.incrementTotalLinks();
                }
                Elements scripts = doc.select("script[src]");
                for (Element s : scripts) {
                    result.addScript(s.attr("abs:src"));
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Unacceptable url: " + url);
            } catch (HttpStatusException e) {
                if (e.getStatusCode() == 404) {
                    System.err.println("GET " + url + " resulted in a 404 (Not Found)");
                } else {
                    System.err.println("GET " + url + " resulted in a " + e.getStatusCode());
                }
            } catch (IOException e) {
                System.err.println("Unable to GET " + url + ": " + e.getMessage());
            }
        }
    }
}
