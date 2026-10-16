package com.fde.GenAI.controllers;

import com.fde.GenAI.models.MovieMatch;
import com.fde.GenAI.services.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/search")
    public List<MovieMatch> search(@RequestParam String query) {
        return movieService.search(query);
    }

    @GetMapping("/{title}/similar")
    public List<MovieMatch> similarMovies(@PathVariable String title) {
        return movieService.similarMovies(title);
    }
}
