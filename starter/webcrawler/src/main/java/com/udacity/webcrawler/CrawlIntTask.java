package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public final class CrawlIntTask extends RecursiveAction{

    Clock clock;
    String url;
    Instant deadline;
    int maxDepth;
    ConcurrentMap<String, Integer> counts;
    Set<String> visitedUrls;
    PageParserFactory parserFactory;

    List<Pattern> ignoredUrls;


    public CrawlIntTask(Clock clock,
                        PageParserFactory parserFactory,
                        int maxDepth,
                        List<Pattern> ignoredUrls,
                        String url,
                        Instant deadline,
                        ConcurrentMap<String, Integer> counts,
                        Set<String> visitedUrls
                        ) {
        this.url=url;
        this.deadline=deadline;
        this.maxDepth=maxDepth;
        this.counts=counts;
        this.visitedUrls=visitedUrls;
        this.clock=clock;
        this.parserFactory=parserFactory;
        this.ignoredUrls=ignoredUrls;

    }

    @Override
    protected void compute() {

        //Return if the maxDepth=0 or time is after the deadline
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        //Does the url matches any in the ignoredUrls
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
        //Same Url should not be visited twice
        if(!visitedUrls.add(url)) {
            return;
        }
        //Removed to make the operation atomic and achieve synchronization
        /*if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);*/

        PageParser.Result result = parserFactory.get(url).parse();
        for (ConcurrentMap.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            counts.compute(e.getKey(), (k, v) -> (v == null) ? e.getValue() : e.getValue() + v);//atomic operation-synchronized
        }

        List<CrawlIntTask> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            subtasks.add(new CrawlIntTask(clock, parserFactory, maxDepth - 1, ignoredUrls, link, deadline,
                    counts, visitedUrls));
        }
        invokeAll(subtasks);
    }
}
