package com.madetech.soheb.moviereviewsbackend.service;

import com.madetech.soheb.moviereviewsbackend.data.*;
import com.madetech.soheb.moviereviewsbackend.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(movieRepository);
    }

    @Test
    @Timeout(5)
    void submitMovie_ValidRequest_ReturnsMovie() {
        MovieSubmissionRequest request = new MovieSubmissionRequest();
        request.setName("Test Movie");
        request.setGenres(List.of("Action", "Drama"));
        request.setDirectors(List.of("Test Director"));
        request.setWriters(List.of("Test Writer"));
        request.setCast(List.of("Test Actor"));
        request.setProducers(List.of("Test Producer"));
        request.setReleaseYear(2023);
        request.setAgeRating(AgeRating.BBFC_15);

        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Movie> result = movieService.submitMovie(request);

        assertTrue(result.isPresent());
        assertEquals("Test Movie", result.get().getName());
        assertEquals(List.of("Action", "Drama"), result.get().getGenres());
        assertEquals(2023, result.get().getReleaseYear());
        assertEquals(AgeRating.BBFC_15, result.get().getAgeRating());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @Timeout(5)
    void getAllMoviesWithRating_ReturnsMovieList() {
        MovieWithRating movie1 = new MovieWithRating();
        movie1.setId(UUID.randomUUID());
        movie1.setName("Movie 1");
        movie1.setAverageRating(8.5);

        MovieWithRating movie2 = new MovieWithRating();
        movie2.setId(UUID.randomUUID());
        movie2.setName("Movie 2");
        movie2.setAverageRating(7.2);

        List<MovieWithRating> movies = Arrays.asList(movie1, movie2);
        when(movieRepository.findAllWithAverageRating()).thenReturn(movies);

        List<MovieWithRating> result = movieService.getAllMoviesWithRating();

        assertEquals(2, result.size());
        assertEquals("Movie 1", result.get(0).getName());
        assertEquals(8.5, result.get(0).getAverageRating());
        verify(movieRepository).findAllWithAverageRating();
    }

    @Test
    @Timeout(5)
    void getAllMoviesWithRating_EmptyList_ReturnsEmptyList() {
        when(movieRepository.findAllWithAverageRating()).thenReturn(List.of());

        List<MovieWithRating> result = movieService.getAllMoviesWithRating();

        assertTrue(result.isEmpty());
        verify(movieRepository).findAllWithAverageRating();
    }

    @Test
    @Timeout(5)
    void findMovieById_ExistingMovie_ReturnsMovie() {
        UUID movieId = UUID.randomUUID();
        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setName("Test Movie");
        movie.setCreatedAt(LocalDateTime.now());

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

        Optional<Movie> result = movieService.findMovieById(movieId);

        assertTrue(result.isPresent());
        assertEquals(movieId, result.get().getId());
        assertEquals("Test Movie", result.get().getName());
        verify(movieRepository).findById(movieId);
    }

    @Test
    @Timeout(5)
    void findMovieById_NonExistingMovie_ReturnsEmpty() {
        UUID movieId = UUID.randomUUID();
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        Optional<Movie> result = movieService.findMovieById(movieId);

        assertFalse(result.isPresent());
        verify(movieRepository).findById(movieId);
    }

    @Test
    @Timeout(5)
    void movieExists_ExistingMovie_ReturnsTrue() {
        UUID movieId = UUID.randomUUID();
        when(movieRepository.existsById(movieId)).thenReturn(true);

        boolean result = movieService.movieExists(movieId);

        assertTrue(result);
        verify(movieRepository).existsById(movieId);
    }

    @Test
    @Timeout(5)
    void movieExists_NonExistingMovie_ReturnsFalse() {
        UUID movieId = UUID.randomUUID();
        when(movieRepository.existsById(movieId)).thenReturn(false);

        boolean result = movieService.movieExists(movieId);

        assertFalse(result);
        verify(movieRepository).existsById(movieId);
    }

    @Test
    @Timeout(5)
    void submitMovie_RepositoryThrowsException_ThrowsRuntimeException() {
        MovieSubmissionRequest request = new MovieSubmissionRequest();
        request.setName("Test Movie");
        request.setGenres(List.of("Action"));
        request.setDirectors(List.of("Test Director"));
        request.setWriters(List.of("Test Writer"));
        request.setCast(List.of("Test Actor"));
        request.setProducers(List.of("Test Producer"));
        request.setReleaseYear(2023);
        request.setAgeRating(AgeRating.BBFC_15);

        when(movieRepository.save(any(Movie.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> movieService.submitMovie(request));
    }
}