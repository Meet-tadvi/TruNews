package newsdetects;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class NewsScraper {
    private static String apiKey;
    private static boolean initialized = false;

    public static void init(ServletContext context) throws IOException {
        if (initialized) return;
        try {
            Properties props = new Properties();
            System.err.println("Servlet context path: " + context.getRealPath("/"));
            System.err.println("Attempting to load /WEB-INF/config.properties");
            InputStream is = context.getResourceAsStream("/WEB-INF/config.properties");
            if (is == null) {
                System.err.println("Error: config.properties not found at /WEB-INF/config.properties");
                throw new IOException("config.properties not found in WEB-INF");
            }
            System.err.println("Loading config.properties from /WEB-INF/config.properties");
            props.load(is);
            is.close();
            apiKey = props.getProperty("news.api.key");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.err.println("Error: news.api.key is missing or empty in config.properties");
                throw new IOException("News API key not configured");
            }
            System.err.println("config.properties loaded successfully. Keys found: " + props.stringPropertyNames());
            initialized = true;
            System.err.println("NewsScraper initialized successfully with API key: " + apiKey.substring(0, 4) + "...");
        } catch (IOException e) {
            System.err.println("Failed to initialize NewsScraper: " + e.getMessage());
            throw e;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static class Article {
        public String title;
        public String content;
        public String url;

        public Article(String title, String content, String url) {
            this.title = title;
            this.content = content;
            this.url = url;
        }
    }

    public static List<Article> fetchTimesOfIndiaNews(String query) throws IOException {
        if (!initialized) {
            throw new IOException("NewsScraper not initialized");
        }

        List<Article> articles = new ArrayList<>();
        String apiUrl = "https://newsapi.org/v2/everything?q=" + java.net.URLEncoder.encode(query, "UTF-8") +
                       "&domains=timesofindia.indiatimes.com,hindustantimes.com,indianexpress.com,ndtv.com" +
                       "&apiKey=" + apiKey + "&language=en&sortBy=publishedAt&pageSize=20";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.err.println("NewsAPI request failed with HTTP " + responseCode);
            throw new IOException("Failed to fetch news from NewsAPI: HTTP " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        conn.disconnect();

        try {
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
            if (!jsonResponse.has("articles")) {
                System.err.println("No articles found in NewsAPI response");
                throw new IOException("No articles found in NewsAPI response");
            }
            JsonArray articlesArray = jsonResponse.getAsJsonArray("articles");

            for (JsonElement element : articlesArray) {
                JsonObject article = element.getAsJsonObject();
                String title = article.has("title") && !article.get("title").isJsonNull() ? article.get("title").getAsString() : "";
                String description = article.has("description") && !article.get("description").isJsonNull() ? article.get("description").getAsString() : "";
                String contentSnippet = article.has("content") && !article.get("content").isJsonNull() ? article.get("content").getAsString() : "";
                String articleUrl = article.has("url") && !article.get("url").isJsonNull() ? article.get("url").getAsString() : "";
                String content = (title + " " + description + " " + contentSnippet).trim();
                if (!content.isEmpty() && !articleUrl.isEmpty()) {
                    articles.add(new Article(title, content, articleUrl));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse NewsAPI response: " + e.getMessage());
            throw new IOException("Failed to parse NewsAPI response: " + e.getMessage());
        }

        System.err.println("Fetched " + articles.size() + " articles for query: " + query);
        return articles;
    }
}