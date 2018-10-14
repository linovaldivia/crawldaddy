# crawldaddy: Yet Another Java 8 web crawler

This project is a multithreaded standalone Java application that uses `RecursiveAction`s in a `ForkJoinPool` along with concurrent data structures to traverse and analyze the pages of a website.

Crawldaddy keeps track of broken links (i.e. links that result in the dreaded HTTP 404 "Not Found" error) as well as external links and external (non-inline imported) scripts used by the site.

Sample output:

    Crawling https://www.mizcracker.com...
    ForkJoinPool.commonPool-worker-2: VISITING: https://www.mizcracker.com
    ForkJoinPool.commonPool-worker-2: VISITING: https://www.mizcracker.com/live/
    ForkJoinPool.commonPool-worker-3: VISITING: https://www.mizcracker.com/about/
    ...
    RESULTS:
    Total number of unique links : 193
       Number of external links  : 158
    Number of broken links       : 0
    Number of ext scripts        : 6
    EXTERNAL LINKS (158): 
    https://www.youtube.com/watch?v=OXY-lYvTMyQ
    https://www.vice.com/en_us/contributor/miz-cracker
    ...
    EXTERNAL SCRIPTS (6): 
    https://static.squarespace.com/universal/scripts-compressed/common-c38b71de3b9638a00d8c-min.en-US.js
    https://www.googletagmanager.com/gtag/js?id=AW-815826109
    ...
    Total crawl time: 5.873 second(s) 


Crawldaddy limits itself to the domain of the input url and will not follow external links (i.e. links that point to resources outside of the domain of the input url).
  
## Running using Maven 3.x

    $ mvn verify
    ...
    $ java -jar target/crawldaddy-0.5-jar-with-dependencies.jar <url-to-crawl>

Example:

    $ java -jar target/crawldaddy-0.5-jar-with-dependencies.jar https://www.mizcracker.com
