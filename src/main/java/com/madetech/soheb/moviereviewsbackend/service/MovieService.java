package com.madetech.soheb.moviereviewsbackend.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.madetech.soheb.moviereviewsbackend.data.Movie;
import com.madetech.soheb.moviereviewsbackend.data.MovieSubmissionRequest;
import com.madetech.soheb.moviereviewsbackend.data.MovieWithRating;
import com.madetech.soheb.moviereviewsbackend.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Optional<Movie> submitMovie(MovieSubmissionRequest request) {
        return executeWithErrorHandling(
                () -> {
                    Movie movie = new Movie();
                    movie.setId(UuidCreator.getTimeOrderedEpoch());
                    movie.setName(request.getName());
                    movie.setGenres(request.getGenres());
                    movie.setDirectors(request.getDirectors());
                    movie.setWriters(request.getWriters());
                    movie.setCast(request.getCast());
                    movie.setProducers(request.getProducers());
                    movie.setReleaseYear(request.getReleaseYear());
                    movie.setAgeRating(request.getAgeRating());
                    movie.setCreatedAt(LocalDateTime.now());

                    return Optional.of(movieRepository.save(movie));
                },
                "ERR_MOVIE_SUBMISSION_FAILED: Failed to submit movie"
        );
    }

    public List<MovieWithRating> getAllMoviesWithRating() {
        return executeWithErrorHandling(
                () -> movieRepository.findAllWithAverageRating(),
                "ERR_MOVIES_RETRIEVAL_FAILED: Failed to retrieve movies"
        );
    }

    public Optional<Movie> findMovieById(UUID movieId) {
        return executeWithErrorHandling(
                () -> movieRepository.findById(movieId),
                "ERR_MOVIE_FIND_FAILED: Failed to find movie by ID"
        );
    }

    public boolean movieExists(UUID movieId) {
        return executeWithErrorHandling(
                () -> movieRepository.existsById(movieId),
                "ERR_MOVIE_EXISTS_CHECK_FAILED: Failed to check if movie exists"
        );
    }

    private <T> T executeWithErrorHandling(Supplier<T> operation, String errorMessage) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage.split(":")[0]);
        }
    }
}