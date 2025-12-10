package newsdetects;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

public class NewsVerifier {
    public static class VerificationResult {
        public boolean isReal;
        public String articleLink;
        public String message;

        public VerificationResult(boolean isReal, String articleLink) {
            this.isReal = isReal;
            this.articleLink = articleLink;
            this.message = null;
        }

        public VerificationResult(boolean isReal, String articleLink, String message) {
            this.isReal = isReal;
            this.articleLink = articleLink;
            this.message = message;
        }
    }

    public VerificationResult verifyNews(String newsText) throws IOException {
        // Handle future events
        if (newsText.toLowerCase().contains("2025") || newsText.toLowerCase().contains("2026")) {
            return new VerificationResult(false, "https://timesofindia.indiatimes.com/india",
                "News appears to reference a future event and cannot be verified.");
        }

        String query = generateQuery(newsText);
        List<NewsScraper.Article> articles = NewsScraper.fetchTimesOfIndiaNews(query);
        // Optional: Fact-check API
        // boolean factCheckResult = verifyWithFactCheckAPI(newsText);
        
        String normalizedInput = preprocessText(newsText);
        Map<String, Double> inputVector = computeTFIDF(normalizedInput);
        Set<String> inputKeywords = extractKeywords(normalizedInput);

        double similarityThreshold = normalizedInput.length() < 50 ? 0.03 : 0.08;
        String defaultLink = "https://timesofindia.indiatimes.com/india";
        double maxSimilarity = 0.0;
        String bestArticleUrl = defaultLink;

        for (NewsScraper.Article article : articles) {
            String normalizedArticle = preprocessText(article.content);
            Map<String, Double> articleVector = computeTFIDF(normalizedArticle);
            Set<String> articleKeywords = extractKeywords(normalizedArticle);

            double similarity = computeCosineSimilarity(inputVector, articleVector);
            int commonKeywords = countCommonKeywords(inputKeywords, articleKeywords);
            System.err.println("Input: " + newsText);
            System.err.println("Article: " + article.url + ", Title: " + article.title + ", Similarity: " + similarity + ", Common Keywords: " + commonKeywords);

            // Tiered verification
            if (/*factCheckResult ||*/ (similarity >= similarityThreshold && commonKeywords >= 2)) {
                return new VerificationResult(true, article.url);
            } else if (similarity >= (similarityThreshold * 0.7) && commonKeywords >= 1) {
                return new VerificationResult(true, article.url);
            }

            if (commonKeywords >= 1 && similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestArticleUrl = article.url;
            }
        }

        return new VerificationResult(false, bestArticleUrl);
    }

    private String generateQuery(String text) {
        String[] words = preprocessText(text).split("\\s+");
        StringBuilder query = new StringBuilder();
        for (String word : words) {
            if (word.length() > 3 && !isStopWord(word)) {
                query.append(word).append(" ");
            }
        }
        String result = query.toString().trim();
        if (result.contains("icc") || result.contains("trophy") || result.contains("champions")) {
            return result + " cricket";
        }
        return result.isEmpty() ? "india" : result;
    }

    private String preprocessText(String text) {
        if (text == null) return "";
        text = text.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 2 && !isStopWord(word)) {
                result.append(word).append(" ");
            }
        }
        return result.toString().trim();
    }

    private Map<String, Double> computeTFIDF(String text) {
        Map<String, Integer> termFreq = new HashMap<>();
        String[] tokens = text.split("\\s+");
        for (String token : tokens) {
            if (!token.isEmpty()) {
                termFreq.put(token, termFreq.getOrDefault(token, 0) + 1);
            }
        }

        Map<String, Double> tfidf = new HashMap<>();
        int docLength = Math.max(tokens.length, 1);
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            double tf = (double) entry.getValue() / docLength;
            double weight = term.matches("india|icc|champions|trophy|2025|win|won|cricket") ? 2.0 : 1.0;
            double idf = Math.log(2.0 / (1 + 1));
            tfidf.put(term, tf * idf * weight);
        }
        return tfidf;
    }

    private double computeCosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : vec1.keySet()) {
            double val1 = vec1.getOrDefault(term, 0.0);
            double val2 = vec2.getOrDefault(term, 0.0);
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
        }

        for (double val : vec2.values()) {
            norm2 += val * val;
        }

        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        return dotProduct / (norm1 * norm2);
    }

    private Set<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.length() > 2 && !isStopWord(word)) {
                keywords.add(word);
            }
        }
        return keywords;
    }

    private int countCommonKeywords(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection.size();
    }

    private boolean isStopWord(String word) {
        String[] stopWords = {"the", "is", "are", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with"};
        return Arrays.asList(stopWords).contains(word);
    }

    /*
    private boolean verifyWithFactCheckAPI(String newsText) throws IOException {
        String apiKey = loadFactCheckApiKey();
        String apiUrl = "https://factchecktools.googleapis.com/v1alpha1/claims:search?query=" +
                        java.net.URLEncoder.encode(newsText, "UTF-8") + "&key=" + apiKey;
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.err.println("FactCheck API failed: HTTP " + responseCode);
            return false;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        conn.disconnect();

        Gson gson = new Gson();
        JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
        JsonArray claims = jsonResponse.getAsJsonArray("claims");
        for (JsonElement claimElement : claims) {
            JsonObject claim = claimElement.getAsJsonObject();
            String claimText = claim.get("text").getAsString();
            JsonArray reviews = claim.getAsJsonArray("claimReview");
            for (JsonElement reviewElement : reviews) {
                JsonObject review = reviewElement.getAsJsonObject();
                String rating = review.get("textualRating").getAsString().toLowerCase();
                if (rating.contains("true") || rating.contains("mostly true")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String loadFactCheckApiKey() throws IOException {
        Properties props = new Properties();
        InputStream is = NewsVerifier.class.getResourceAsStream("/config.properties");
        if (is == null) {
            throw new IOException("config.properties not found");
        }
        props.load(is);
        is.close();
        String apiKey = props.getProperty("factcheck.api.key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("FactCheck API key not configured");
        }
        return apiKey;
    }
    */
}