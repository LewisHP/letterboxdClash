package com.lewis.letterboxdClash.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lewis.letterboxdClash.entity.Film;

@Service
public class FilmService {

    @Autowired
    private TMDBService tmdbService;

    private static final String BASE_URL = "https://letterboxd.com";
    private static final int TIMEOUT = 10000; // 10 seconds

    /**
     * Scrapes films from a Letterboxd user's film page.
     * Iterates through all pages to collect film data including images, titles, and ratings.
     *
     * @param username The Letterboxd username
     * @return List of Film objects containing title, image URL, and rating
     */
    public List<Film> getFilms(String username) {
        List<Film> allFilms = new ArrayList<>();
        int currentPage = 1;
        boolean hasMorePages = true;

        while (hasMorePages) {
            try {
                String url = buildUrl(username, currentPage);
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(TIMEOUT)
                        .get();

                // Extract films from current page
                List<Film> pageFilms = extractFilmsFromPage(doc);
                allFilms.addAll(pageFilms);

                // Check if there's a next page
                hasMorePages = hasNextPage(doc);
                currentPage++;

            } catch (IOException e) {
                System.err.println("Error scraping page " + currentPage + ": " + e.getMessage());
                hasMorePages = false;
            }
        }

        // Don't fetch posters here - will be fetched on-demand per round
        return allFilms;
    }

    /**
     * Fetches poster URL from TMDB for a specific film
     *
     * @param film The film to get poster for
     * @return Film with poster URL populated
     */
    public Film getFilmWithPoster(Film film) {
        String posterUrl = tmdbService.getPosterUrl(film.getTitle());
        film.setImage(posterUrl);
        return film;
    }

    /**
     * Builds the URL for a specific page of a user's films
     */
    private String buildUrl(String username, int page) {
        if (page == 1) {
            return BASE_URL + "/" + username + "/films/";
        }
        return BASE_URL + "/" + username + "/films/page/" + page + "/";
    }

    /**
     * Extracts film data from a single page
     */
    private List<Film> extractFilmsFromPage(Document doc) {
        List<Film> films = new ArrayList<>();

        // Letterboxd uses li.griditem for each film in the grid
        Elements filmElements = doc.select("li.griditem");

        System.out.println("DEBUG: Found " + filmElements.size() + " film elements");

        for (Element filmElement : filmElements) {
            try {
                String title = extractTitle(filmElement);
                String imageUrl = extractImageUrl(filmElement);
                Double rating = extractRating(filmElement);

                //System.out.println("DEBUG: Extracted film - Title: " + title + ", Image: " + imageUrl + ", Rating: " + rating);
                films.add(new Film(title, imageUrl, rating));
            } catch (Exception e) {
                System.err.println("Error extracting film data: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return films;
    }

    /**
     * Extracts the film title from a film element
     */
    private String extractTitle(Element filmElement) {
        // Try data-item-name attribute from react-component div
        Element reactComponent = filmElement.selectFirst("div.react-component");
        if (reactComponent != null) {
            String dataItemName = reactComponent.attr("data-item-name");
            if (dataItemName != null && !dataItemName.isEmpty()) {
                return dataItemName;
            }
        }

        // Fallback to img alt attribute
        Element imgElement = filmElement.selectFirst("img");
        if (imgElement != null) {
            String alt = imgElement.attr("alt");
            if (alt != null && !alt.isEmpty()) {
                return alt;
            }
        }
        return "Unknown";
    }

    /**
     * Extracts the film poster image URL using TMDB API
     * Note: The actual URL will be populated after fetching from TMDB in getFilms()
     */
    private String extractImageUrl(Element filmElement) {
        // Return placeholder - will be replaced by TMDB poster URL later
        return null;
    }

    /**
     * Extracts the user's rating for the film (if available)
     */
    private Double extractRating(Element filmElement) {
        Element ratingElement = filmElement.selectFirst("span.rating");
        if (ratingElement != null) {
            String ratingClass = ratingElement.attr("class");
            // Letterboxd uses classes like "rated-10" for 5 stars, "rated-8" for 4 stars, etc.
            if (ratingClass.contains("rated-")) {
                String ratingStr = ratingClass.replaceAll(".*rated-(\\d+).*", "$1");
                try {
                    int ratingValue = Integer.parseInt(ratingStr);
                    // Convert from 0-10 scale to 0-5 stars
                    return ratingValue / 2.0;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Checks if there's a next page available
     */
    private boolean hasNextPage(Document doc) {
        Element nextLink = doc.selectFirst("a.next");
        return nextLink != null;
    }
}
