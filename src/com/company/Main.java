package com.company;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//using soup
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

    // got it online no inventing
    private static String EMAIL_REGEX = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";

    //visited
    private static HashSet<String> linkHashSet = new HashSet<>();

    //emails
    private static HashSet<String> emailSet = new HashSet<>();

    private static String emailSpace;

    //originalAddress address
    private static String originalAddress;

    //crawl layer, 2 takes long but 2+ gonna take longer
    //with 1 mit only returned 1 email so testing with 2
    private static int LAYER_LIMIT = 2;

    public static void main(String[] args) {
	    //Empty Args, uncomment for test
        if (args.length == 0){
            return;
        }

        System.out.println("Found these email addresses:");

        originalAddress = args[0];

        //Testing
        //originalAddress = "www.jana.com";

        emailSpace = emailSpace(removePathAndQueryParam(originalAddress));

        findPagesAndCheckEmail(originalAddress, 0);

        Iterator entryIter = emailSet.iterator();

        while (entryIter.hasNext()) {
            String entry = (String)entryIter.next();
            System.out.println(entry);
        }
    }

    private static void findPagesAndCheckEmail(String web, int layer){
        linkHashSet.add(web);

        web = "http://" + web;

        try {

            Document document = Jsoup.connect(web).get();

            Elements linksOnPage = document.select("a[href]");

            for (Element page : linksOnPage) {
                String pageLink = getSimpleAddress(page.attr("abs:href"));
                //Parse out emails
                if (pageLink.contains("mailto")){
                    //Just in case
                    pageLink = java.net.URLDecoder.decode(pageLink, "UTF-8");
                    Matcher m = Pattern.compile(EMAIL_REGEX).matcher(pageLink);

                    while (m.find()) {
                        String email = m.group();
                        if (email.contains(emailSpace) && !emailSet.contains(email)) {
                            emailSet.add(email);
                        }
                    }
                } else {
                    // Test links
                    if (layer < LAYER_LIMIT) {
                        if (pageLink.startsWith(originalAddress) && !linkHashSet.contains(pageLink)) {
                            findPagesAndCheckEmail(pageLink, layer++);
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private static String removePathAndQueryParam(String web){
        String subWeb = web;
        while (subWeb.lastIndexOf("/") != -1){
            subWeb = subWeb.substring(0,subWeb.lastIndexOf("/"));
        }

        while (subWeb.lastIndexOf("?") != -1){
            subWeb = subWeb.substring(0,subWeb.lastIndexOf("?"));
        }
        return subWeb;
    }

    private static String emailSpace(String web){
        String[] splitString = web.split("\\.");
        return "@" + splitString[splitString.length-2] + "." +  splitString[splitString.length-1];
    }

    private static String getSimpleAddress(String web){
        web = web.replace("http://", "");
        web = web.replace("https://", "");
        web = web.replace("//", "");
        return web;
    }

}
