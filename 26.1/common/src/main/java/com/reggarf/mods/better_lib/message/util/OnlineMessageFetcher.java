package com.reggarf.mods.better_lib.message.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Generic HTTP text fetcher for Better Lib’s online message system.
 * Supports multi-line text and safe fallback.
 */
public class OnlineMessageFetcher {

    public static String fetchOnlineMessage(String urlString) {
        if (urlString == null || urlString.isEmpty()) return "";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString().trim();
            }
        } catch (Exception e) {
            System.err.println("[BetterLib] Failed to fetch online message: " + e.getMessage());
            return "";
        }
    }
}
