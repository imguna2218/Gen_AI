package com.fde.GenAI.services;

import com.fde.GenAI.models.Movie;
import com.fde.GenAI.models.MovieData;
import com.fde.GenAI.models.MovieMatch;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class MovieService {
    private final EmbeddingModel embeddingModel;

    private List<Movie> moviesEmbedding = new ArrayList<>();

    @Autowired
    private JsonMapper jsonMapper;

    public MovieService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<MovieMatch> search(String query) {
        float[] userQueryEmbedding = embeddingModel.embed(query);

        List<MovieMatch> matches = new ArrayList<>();

        for(Movie movie : moviesEmbedding) {
            double similarity = cosineSimilarity(userQueryEmbedding, movie.getEmbedding());

            MovieMatch match = new MovieMatch(movie.getTitle(), movie.getDescription(), similarity);

            matches.add(match);
        }

        sortBySimilarity(matches);

        return topKMatches(matches, 3);
    }

    //@PostConstruct
    public void initializeMovies() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("movies.json");

        InputStream inputStream =
                resource.getInputStream();

        List<MovieData> movieDataList = jsonMapper.readValue(
                inputStream,
                new TypeReference<>() {}
        );

        for (MovieData movieData : movieDataList) {

            float[] embedding = embeddingModel.embed(
                    movieData.getDescription()
            );

            Movie movie = new Movie(
                    movieData.getTitle(),
                    movieData.getDescription(),
                    embedding
            );

            moviesEmbedding.add(movie);
        }

        inputStream.close();

        System.out.println(
                moviesEmbedding.size() +
                        " movies loaded with embeddings."
        );

        for (Movie movie : moviesEmbedding) {
            System.out.println(
                    Arrays.toString(movie.getEmbedding())
            );
        }
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += (a[i] * b[i]);
            normA += (a[i] * a[i]);
            normB += (b[i] * b[i]);
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private void sortBySimilarity(List<MovieMatch> matches) {
        Collections.sort(matches,
                (first, second) -> Double.compare(
                        second.getMatch(),
                        first.getMatch())
        );
    }

    private List<MovieMatch> topKMatches(
            List<MovieMatch> matches, int limit) {

        List<MovieMatch> topMatches = new ArrayList<>();

        int numberOfMatches = Math.min(limit, matches.size());

        for (int i = 0; i < numberOfMatches; i++) {
            topMatches.add(matches.get(i));
        }

        return topMatches;
    }

    public List<MovieMatch> similarMovies(String title) {
        Movie selectedMovie = findMovie(title);
        List<MovieMatch> matches = new ArrayList<>();

        for (Movie movie : moviesEmbedding) {
            if (movie.getTitle().equalsIgnoreCase(title)) {
                continue;
            }

            double similarity = cosineSimilarity(
                    selectedMovie.getEmbedding(),
                    movie.getEmbedding());

            MovieMatch match = new MovieMatch(
                    movie.getTitle(),
                    movie.getDescription(),
                    similarity);

            matches.add(match);
        }
        sortBySimilarity(matches);
        return topKMatches(matches, 3);
    }

    private Movie findMovie(String title) {
        for (Movie movie : moviesEmbedding) {
            if (movie.getTitle().equalsIgnoreCase(title)) {
                return movie;
            }
        }

        throw new IllegalArgumentException(
                "Movie not found: " + title
        );
    }

}
