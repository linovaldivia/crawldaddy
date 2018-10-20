package org.lagalag.crawldaddy.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupPageFetchService implements PageFetchService {
    private static final String LINK_ELEMENT_SELECTOR = "a[href]";
    private static final String SCRIPT_ELEMENT_SELECTOR = "script[src]";
    private static final String ABSOLUTE_HREF_ATTRIBUTE = "abs:href";
    private static final String ABSOLUTE_SRC_ATTRIBUTE = "abs:src";

    @Override
    public void fetch(String url, PageFetchConsumer resultsConsumer) throws PageFetchException {
        PageFetchResults results = null;
        try {
            Document doc = Jsoup.connect(url).get();
            Elements linkElements = doc.select(LINK_ELEMENT_SELECTOR);
            List<String> links = extractAttributeValues(linkElements, ABSOLUTE_HREF_ATTRIBUTE);
            Elements scriptElements = doc.select(SCRIPT_ELEMENT_SELECTOR);
            List<String> scripts = extractAttributeValues(scriptElements, ABSOLUTE_SRC_ATTRIBUTE);
            results = PageFetchResults.onHttpOK(url, links, scripts);
        } catch (IllegalArgumentException e) {
            throw new PageFetchException("Detected malformed url: " + url);
        } catch (HttpStatusException e) {
            results = PageFetchResults.onHttpNotOK(url, e.getStatusCode());
        } catch (IOException e) {
            throw new PageFetchException("Unable to GET " + url + ": " + e.getMessage());
        }
        resultsConsumer.handlePageFetchResults(results);
    }
    
    private List<String> extractAttributeValues(Elements elements, String attrName) {
        List<String> attrs = new ArrayList<>(elements.size());
        for (Element e : elements) {
            attrs.add(e.attr(attrName));
        }
        return attrs;
    }
}
