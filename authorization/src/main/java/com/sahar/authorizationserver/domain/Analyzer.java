package com.sahar.authorizationserver.domain;

import nl.basjes.parse.useragent.UserAgentAnalyzer;


// This class is responsible for providing a singleton instance of UserAgentAnalyzer
// Used to analyze user-agent strings (to detect device type, browser, OS, etc.)
public class Analyzer {
    private static UserAgentAnalyzer INSTANCE;

    // Public method to access the singleton instance
    public static UserAgentAnalyzer getInstance() {
        if(INSTANCE == null) {
            INSTANCE = UserAgentAnalyzer
                    .newBuilder()
                    .hideMatcherLoadStats() // Optional: Hides matcher loading statistics (keeps logs clean)
                    .withCache(10000)  // Use a cache to improve performance (stores 10,000 parsed results)
                    .build();  // Build the final UserAgentAnalyzer object
        }
        return INSTANCE;  // Return the same instance every time (singleton)
    }
}