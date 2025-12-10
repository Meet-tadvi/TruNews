package newsdetects;

import jakarta.servlet.ServletContext;

import java.io.*;
import java.util.*;

public class MLNewsVerifier {
    private static boolean initialized = false;
    private static List<NewsEntry> newsEntries = new ArrayList<>();

    // Class to store news entries from fake_news_data.txt
    private static class NewsEntry {
        String label; // FAKE or REAL
        String text;
        Set<String> keywords;

        NewsEntry(String label, String text) {
            this.label = label;
            this.text = text;
            this.keywords = extractKeywords(text);
        }
    }

    public static class VerificationResult {
        public String prediction;
        public double confidence;

        public VerificationResult(String prediction, double confidence) {
            this.prediction = prediction;
            this.confidence = confidence;
        }
    }

    public static void init(ServletContext context) throws Exception {
        if (initialized) return;

        System.out.println("Initializing MLNewsVerifier...");

        try {
            // Load fake_news_data.txt from classpath
            String filePath = "fake_news_data.txt";
            System.out.println("Attempting to load resource: " + filePath);
            InputStream inputStream = MLNewsVerifier.class.getClassLoader().getResourceAsStream(filePath);
            if (inputStream == null) {
                System.err.println("Error: Resource not found: " + filePath);
                throw new IOException("Cannot find resource: " + filePath);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int lineCount = 0;
            int validEntries = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (line.trim().isEmpty()) continue; // Skip empty lines
                String[] parts = line.split(":::");
                if (parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
                    String label = parts[0].trim().toUpperCase();
                    if (label.equals("FAKE") || label.equals("REAL")) {
                        newsEntries.add(new NewsEntry(label, parts[1].trim()));
                        validEntries++;
                    } else {
                        System.err.println("Warning: Invalid label at line " + lineCount + ": " + line);
                    }
                } else {
                    System.err.println("Warning: Invalid format at line " + lineCount + ": " + line);
                }
            }
            reader.close();
            inputStream.close();

            if (validEntries == 0) {
                throw new IOException("No valid entries found in fake_news_data.txt");
            }
            System.out.println("Loaded " + validEntries + " valid entries from fake_news_data.txt");

            initialized = true;
            System.out.println("MLNewsVerifier initialized successfully.");
        } catch (Exception e) {
            System.err.println("MLNewsVerifier initialization failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static VerificationResult verifyNews(String text, ServletContext context) throws Exception {
        init(context);

        if (text == null || text.trim().isEmpty()) {
            return new VerificationResult("Unverified", 0.0);
        }

        // Preprocess input text
        String normalizedInput = preprocessText(text);
        Set<String> inputKeywords = extractKeywords(normalizedInput);

        // Find the best match
        double maxSimilarity = 0.0;
        String bestLabel = "Unverified";
        for (NewsEntry entry : newsEntries) {
            double similarity = computeSimilarity(inputKeywords, entry.keywords);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestLabel = entry.label;
            }
        }

        // Threshold for considering a match
        double confidence = maxSimilarity;
        if (maxSimilarity < 0.3) { // Adjust threshold as needed
            bestLabel = "Unverified";
            confidence = 0.0;
        }

        return new VerificationResult(bestLabel, confidence);
    }

    public static boolean isInitialized() {
        return initialized;
    }

    // Preprocess text: lowercase, remove punctuation, remove stopwords
    private static String preprocessText(String text) {
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

    // Extract keywords as a set
    private static Set<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                keywords.add(word);
            }
        }
        return keywords;
    }

    // Compute similarity based on keyword overlap
    private static double computeSimilarity(Set<String> set1, Set<String> set2) {
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size(); // Jaccard similarity
    }

    // Check if a word is a stopword
    private static boolean isStopWord(String word) {
        String[] stopWords = {"the", "is", "are", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with"};
        return Arrays.asList(stopWords).contains(word);
    }
}