package com.lewis.letterboxdClash.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lewis.letterboxdClash.entity.Film;
import com.lewis.letterboxdClash.service.FilmService;

@RestController
@RequestMapping("/api/films")
public class FilmController {

    @Autowired
    private FilmService filmService;

    /**
     * Endpoint to fetch all films for a given Letterboxd username
     * Example: GET /api/films/dave
     *
     * @param username The Letterboxd username
     * @return List of Film objects with title and rating (no posters)
     */
    @GetMapping("/{username}")
    public List<Film> getFilmsByUsername(@PathVariable String username) {
        System.out.println("=== Getting films for username: " + username + " ===");
        List<Film> films = filmService.getFilms(username);
        System.out.println("Total films found: " + films.size());
        long ratedFilms = films.stream().filter(f -> f.getRating() != null).count();
        System.out.println("Films with ratings: " + ratedFilms);
        return films;
    }

    /**
     * Endpoint to fetch posters for specific films
     * Example: POST /api/films/posters
     * Body: [{"title": "Film 1", "rating": 4.5}, {"title": "Film 2", "rating": 3.0}]
     *
     * @param films List of films to get posters for
     * @return List of films with poster URLs populated
     */
    @PostMapping("/posters")
    public List<Film> getPostersForFilms(@RequestBody List<Film> films) {
        System.out.println("=== POSTERS ENDPOINT CALLED with " + films.size() + " films ===");
        return films.stream()
                .map(film -> {
                    System.out.println("Processing film: " + film.getTitle());
                    filmService.getFilmWithPoster(film);
                    return film;
                })
                .filter(film -> film.getImage() != null) // Only return films with posters
                .collect(Collectors.toList());
    }
}
