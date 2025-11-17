package com.lewis.letterboxdClash.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class TMDBService {

    private final String apiKey;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    private final RestTemplate restTemplate;
    private final Map<String, String> posterCache = new HashMap<>();

    @Autowired
    public TMDBService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.apiKey = System.getenv("TMDB_API_KEY");
    }

    /**
     * Searches for a film by title and returns the poster URL
     * Caches results to avoid redundant API calls
     *
     * @param title The film title (may include year)
     * @return Poster URL or null if not found
     */
    public String getPosterUrl(String title) {
        // Check cache first
        if (posterCache.containsKey(title)) {
            return posterCache.get(title);
        }

        try {
            // Clean title - remove year if present (e.g., "Film (2024)" -> "Film")
            String cleanTitle = title.replaceAll("\\s*\\(\\d{4}\\)\\s*$", "").trim();

            // Build search URL
            String searchUrl = TMDB_BASE_URL + "/search/movie"
                    + "?api_key=" + apiKey
                    + "&query=" + cleanTitle.replace(" ", "%20");

            // Make API call
            JsonNode response = restTemplate.getForObject(searchUrl, JsonNode.class);

            if (response != null && response.has("results")) {
                JsonNode results = response.get("results");

                if (results.isArray() && results.size() > 0) {
                    // Get first result (best match)
                    JsonNode firstResult = results.get(0);

                    if (firstResult.has("poster_path") && !firstResult.get("poster_path").isNull()) {
                        String posterPath = firstResult.get("poster_path").asText();
                        String posterUrl = TMDB_IMAGE_BASE_URL + posterPath;

                        // Cache the result
                        posterCache.put(title, posterUrl);
                        return posterUrl;
                    }
                }
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Fetches posters for multiple films in batch
     * Useful for initial loading
     *
     * @param titles List of film titles
     * @return Map of title -> poster URL
     */
    public Map<String, String> getPostersForFilms(List<String> titles) {
        Map<String, String> posters = new HashMap<>();

        for (String title : titles) {
            String posterUrl = getPosterUrl(title);
            if (posterUrl != null) {
                posters.put(title, posterUrl);
            }
        }

        return posters;
    }

    /**
     * Clears the poster cache
     */
    public void clearCache() {
        posterCache.clear();
    }
}
