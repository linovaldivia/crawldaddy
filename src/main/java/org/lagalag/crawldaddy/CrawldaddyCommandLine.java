package org.lagalag.crawldaddy;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Handles parsing and management of command line arguments and options.
 *
 */
public class CrawldaddyCommandLine {
    public static final String CL_OPT_SHOW_EXT_LINKS = "xl";
    public static final String CL_OPT_SHOW_EXT_SCRIPTS = "xs";
    public static final String CL_OPT_MAX_INT_LINKS = "m";
    public static final String CL_LONGOPT_SHOW_EXT_LINKS = "extLinks";
    public static final String CL_LONGOPT_SHOW_EXT_SCRIPTS = "extScripts";
    public static final String CL_LONGOPT_MAX_INT_LINKS = "maxIntLinks";

    private static final Options COMMAND_LINE_OPTIONS;
    static {
        COMMAND_LINE_OPTIONS = new Options();
        COMMAND_LINE_OPTIONS.addOption(Option.builder(CL_OPT_MAX_INT_LINKS).longOpt(CL_LONGOPT_MAX_INT_LINKS).hasArg().argName("MAX")
                                             .desc("Limit number of internal links followed (default=" + CrawldaddyParams.DEFAULT_MAX_INTERNAL_LINKS + ").").build());
        COMMAND_LINE_OPTIONS.addOption(Option.builder(CL_OPT_SHOW_EXT_SCRIPTS).longOpt(CL_LONGOPT_SHOW_EXT_SCRIPTS)
                                             .desc("Show external scripts encountered.").build());
        COMMAND_LINE_OPTIONS.addOption(Option.builder(CL_OPT_SHOW_EXT_LINKS).longOpt(CL_LONGOPT_SHOW_EXT_LINKS)
                                             .desc("Show external links encountered.").build());
    }
    
    private CommandLine commandLine;
    private String inputUrl;

    public static void showHelp(PrintStream out) {
        showHelp(new PrintWriter(out));
    }
    
    public static void showHelp(PrintWriter out) {
        HelpFormatter help = new HelpFormatter();
        help.printHelp(out, HelpFormatter.DEFAULT_WIDTH, "crawldaddy [options] url-to-crawl", 
                       "Crawl a website to look for broken links, external script references, etc.",
                       COMMAND_LINE_OPTIONS, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null);
        out.flush();
    }
    
    public static CrawldaddyCommandLine parse(String[] args) {
        if (args.length == 0) {
            return null;
        }

        try {
            CommandLineParser clParser = new DefaultParser();
            return new CrawldaddyCommandLine(clParser.parse(COMMAND_LINE_OPTIONS, args));
        } catch(ParseException exp) {
            System.err.println(exp.getMessage());
        }
        return null;
    }
    
    private CrawldaddyCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
        if (commandLine.getArgs().length > 0) {
            this.inputUrl = commandLine.getArgs()[0];
        }
    }
    
    public String getInputUrl() {
        return this.inputUrl;
    }
    
    public Integer getMaxInternalLinks() {
        return getUnsignedIntValue(commandLine.getOptionValue(CL_OPT_MAX_INT_LINKS));
    }
    
    public boolean isShowExternalLinksSet() {
        return this.commandLine.hasOption(CL_OPT_SHOW_EXT_LINKS);
    }
    
    public boolean isShowExternalScriptsSet() {
        return this.commandLine.hasOption(CL_OPT_SHOW_EXT_SCRIPTS);
    }
    
    private Integer getUnsignedIntValue(String stringValue) {
        if (stringValue == null) {
            return null;
        }
        stringValue = stringValue.trim();
        try {
            return Integer.parseUnsignedInt(stringValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
