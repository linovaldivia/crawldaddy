package org.lagalag.crawldaddy;

import java.io.IOException;
import java.util.concurrent.RecursiveTask;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawldaddyTask extends RecursiveTask<CrawldaddyResult> {
    private static final long serialVersionUID = 1L;
    private String url;
    
    public CrawldaddyTask(String url) {
        this.url = url;
    }
    
    @Override
    protected CrawldaddyResult compute() {
        if (url != null) {
            try {
                Document doc = Jsoup.connect(url).get();
                
                CrawldaddyResult cdr = new CrawldaddyResult(url);
                Elements ahrefs = doc.select("a[href]");
                for (Element e : ahrefs) {
                    // TODO increment only for each link that has been visited (even attempted visits to broken links)
                    cdr.incrementTotalLinks();
                }
                Elements scripts = doc.select("script[src]");
                for (Element s : scripts) {
                    cdr.addScript(s.attr("abs:src"));
                }
                
                return cdr;
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
        
        return null;
    }
}
