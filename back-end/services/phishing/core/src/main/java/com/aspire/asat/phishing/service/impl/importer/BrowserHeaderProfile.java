package com.aspire.asat.phishing.service.impl.importer;

import java.util.Map;

public record BrowserHeaderProfile(String name, Map<String, String> headers) {

    public static BrowserHeaderProfile chromeWindows() {
        return new BrowserHeaderProfile("ChromeWindows", Map.ofEntries(
                Map.entry("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"),
                Map.entry("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,"
                        + "image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"),
                Map.entry("Accept-Language", "en-US,en;q=0.9"),
                Map.entry("Accept-Encoding", "gzip, deflate"),
                Map.entry("Cache-Control", "max-age=0"),
                Map.entry("Sec-Fetch-Dest", "document"),
                Map.entry("Sec-Fetch-Mode", "navigate"),
                Map.entry("Sec-Fetch-Site", "none"),
                Map.entry("Sec-Fetch-User", "?1"),
                Map.entry("Upgrade-Insecure-Requests", "1"),
                Map.entry("Sec-Ch-Ua", "\"Not A(Brand\";v=\"99\", \"Chromium\";v=\"121\", \"Google Chrome\";v=\"121\""),
                Map.entry("Sec-Ch-Ua-Mobile", "?0"),
                Map.entry("Sec-Ch-Ua-Platform", "\"Windows\"")
        ));
    }

    public static BrowserHeaderProfile edgeWindows() {
        return new BrowserHeaderProfile("EdgeWindows", Map.of(
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,"
                        + "image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
                "Accept-Language", "en-US,en;q=0.9",
                "Accept-Encoding", "gzip, deflate",
                "Cache-Control", "max-age=0",
                "Sec-Fetch-Dest", "document",
                "Sec-Fetch-Mode", "navigate",
                "Sec-Fetch-Site", "none",
                "Sec-Fetch-User", "?1",
                "Upgrade-Insecure-Requests", "1"
        ));
    }

    public static BrowserHeaderProfile firefox() {
        return new BrowserHeaderProfile("Firefox", Map.of(
                "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:121.0) "
                        + "Gecko/20100101 Firefox/121.0",
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                "Accept-Language", "en-US,en;q=0.5",
                "Accept-Encoding", "gzip, deflate",
                "Cache-Control", "no-cache",
                "Sec-Fetch-Dest", "document",
                "Sec-Fetch-Mode", "navigate",
                "Sec-Fetch-Site", "none",
                "Sec-Fetch-User", "?1",
                "Upgrade-Insecure-Requests", "1"
        ));
    }
}

