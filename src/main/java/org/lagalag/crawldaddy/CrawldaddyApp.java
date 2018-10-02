package org.lagalag.crawldaddy;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Main command-line application class: handles command-line parsing, displaying output, etc.
 *
 */
public class CrawldaddyApp {
    private static final String CL_LONGOPT_SHOW_EXT_LINKS = "extLinks";
    private static final String CL_LONGOPT_SHOW_EXT_SCRIPTS = "extScripts";
    private static final String CL_LONGOPT_MAX_INT_LINKS = "maxIntLinks";
    private static final String CL_OPT_SHOW_EXT_LINKS = "xl";
    private static final String CL_OPT_SHOW_EXT_SCRIPTS = "xs";
    private static final String CL_OPT_MAX_INT_LINKS = "m";

    private void showResults(CrawldaddyResult result, CommandLine cl) {
        if (result == null) {
            return;
        }
        Set<String> extLinks = result.getExternalLinks();
        Set<String> brokenLinks = result.getBrokenLinks();
        Set<String> extScripts = result.getExternalScripts();
        
        System.out.println("RESULTS for " + result.getUrl() + ":");
        System.out.println("Total number of unique links : " + result.getTotalLinkCount());
        System.out.println("   Number of external links  : " + extLinks.size());
        System.out.println("Number of broken links       : " + brokenLinks.size());
        System.out.println("Number of ext scripts        : " + extScripts.size());
        if (cl.hasOption(CL_OPT_SHOW_EXT_LINKS)) {
            showSetContents("EXTERNAL LINKS", extLinks);
        }
        showSetContents("BROKEN LINKS", brokenLinks);
        if (cl.hasOption(CL_OPT_SHOW_EXT_SCRIPTS)) {
            showSetContents("EXTERNAL SCRIPTS", extScripts);
        }
        showCrawlTime(result.getCrawlTime());
    }
    
    private void showSetContents(String title, Set<String> set) {
        if (set.size() > 0) {
            System.out.println(title + " (" + set.size() + "): ");
            for (String s : set) {
                System.out.println(s);
            }
        }
    }
    
    private void showCrawlTime(Duration crawlTime) {
        StringBuilder ct = new StringBuilder("Total crawl time: ");
        if (crawlTime.toHours() > 0) {
            ct.append(crawlTime.toHours()).append(" hour(s) ");
        } else if (crawlTime.toMinutes() > 0 ) {
            ct.append(crawlTime.toMinutes()).append(" minute(s) ");
        } 
        long millis = crawlTime.toMillis();
        long secs =  millis / 1000;
        millis %= 1000;
        if (secs > 0) {
            ct.append(secs).append(".").append(millis).append(" second(s) ");
        }
        System.out.println(ct.toString());
    }
    
    private Options createCLOptions() {
        Options options = new Options();
        options.addOption(Option.builder(CL_OPT_MAX_INT_LINKS).longOpt(CL_LONGOPT_MAX_INT_LINKS).hasArg().argName("MAX")
                                .desc("Limit number of internal links followed (default=" + Crawldaddy.DEFAULT_MAX_INTERNAL_LINKS + ").").build());
        options.addOption(Option.builder(CL_OPT_SHOW_EXT_SCRIPTS).longOpt(CL_LONGOPT_SHOW_EXT_SCRIPTS)
                                .desc("Show external scripts encountered.").build());
        options.addOption(Option.builder(CL_OPT_SHOW_EXT_LINKS).longOpt(CL_LONGOPT_SHOW_EXT_LINKS)
                          .desc("Output only external links encountered.").build());
        return options;
    }
    
    private void showHelpAndExit(Options clOptions) {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("crawldaddy [options] url-to-crawl", clOptions);
        System.exit(1);
    }
    
    private int getMaxNumInternalLinks(String stringValue, int defaultValue) {
        try {
            return Integer.parseUnsignedInt(stringValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public void runApp(String[] args) {
        Options clOptions = createCLOptions();
        if (args.length == 0) {
            showHelpAndExit(clOptions);
        }
        
        String urlToCrawl = null;
        int maxIntLinks = Crawldaddy.DEFAULT_MAX_INTERNAL_LINKS;
        
        try {
            CommandLineParser clParser = new DefaultParser();
            CommandLine cl = clParser.parse(clOptions, args);
            if (cl.getArgs().length > 0) {
                urlToCrawl = cl.getArgs()[0];
            }
            if (cl.hasOption(CL_OPT_MAX_INT_LINKS)) {
                maxIntLinks = getMaxNumInternalLinks(cl.getOptionValue(CL_OPT_MAX_INT_LINKS), Crawldaddy.DEFAULT_MAX_INTERNAL_LINKS);
            }
            
            System.out.println("Crawling " + urlToCrawl + "...");
            Future<CrawldaddyResult> fresult = new Crawldaddy(urlToCrawl, maxIntLinks).startCrawl();
            if (fresult != null) {
                try {
                    showResults(fresult.get(), cl);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch(ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        new CrawldaddyApp().runApp(args);
    }
}
