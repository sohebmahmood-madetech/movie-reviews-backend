package com.madetech.soheb.moviereviewsbackend.repository;

import com.madetech.soheb.moviereviewsbackend.data.AgeRating;
import com.madetech.soheb.moviereviewsbackend.data.Movie;
import com.madetech.soheb.moviereviewsbackend.data.MovieWithRating;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MovieRepository {

    private final JdbcTemplate jdbcTemplate;

    public MovieRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Movie> MOVIE_ROW_MAPPER = new RowMapper<Movie>() {
        @Override
        public Movie mapRow(ResultSet rs, int rowNum) throws SQLException {
            Movie movie = new Movie();
            movie.setId(UUID.fromString(rs.getString("id")));
            movie.setName(rs.getString("name"));
            movie.setGenres(Arrays.asList((String[]) rs.getArray("genres").getArray()));
            movie.setDirectors(Arrays.asList((String[]) rs.getArray("directors").getArray()));
            movie.setWriters(Arrays.asList((String[]) rs.getArray("writers").getArray()));
            movie.setCast(Arrays.asList((String[]) rs.getArray("cast").getArray()));
            movie.setProducers(Arrays.asList((String[]) rs.getArray("producers").getArray()));
            movie.setReleaseYear(rs.getInt("release_year"));
            movie.setAgeRating(AgeRating.valueOf(rs.getString("age_rating")));
            movie.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return movie;
        }
    };

    private static final RowMapper<MovieWithRating> MOVIE_WITH_RATING_ROW_MAPPER = new RowMapper<MovieWithRating>() {
        @Override
        public MovieWithRating mapRow(ResultSet rs, int rowNum) throws SQLException {
            MovieWithRating movie = new MovieWithRating();
            movie.setId(UUID.fromString(rs.getString("id")));
            movie.setName(rs.getString("name"));
            movie.setGenres(Arrays.asList((String[]) rs.getArray("genres").getArray()));
            movie.setDirectors(Arrays.asList((String[]) rs.getArray("directors").getArray()));
            movie.setWriters(Arrays.asList((String[]) rs.getArray("writers").getArray()));
            movie.setCast(Arrays.asList((String[]) rs.getArray("cast").getArray()));
            movie.setProducers(Arrays.asList((String[]) rs.getArray("producers").getArray()));
            movie.setReleaseYear(rs.getInt("release_year"));
            movie.setAgeRating(AgeRating.valueOf(rs.getString("age_rating")));
            movie.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            Double avgRating = rs.getObject("average_rating", Double.class);
            movie.setAverageRating(avgRating);
            return movie;
        }
    };

    public Optional<Movie> findById(UUID id) {
        String sql = "SELECT * FROM movies WHERE id = ?";
        List<Movie> movies = jdbcTemplate.query(sql, MOVIE_ROW_MAPPER, id);
        return movies.isEmpty() ? Optional.empty() : Optional.of(movies.get(0));
    }

    public List<MovieWithRating> findAllWithAverageRating() {
        String sql = """
            SELECT m.*, AVG(r.rating) as average_rating
            FROM movies m
            LEFT JOIN reviews r ON m.id = r.movie_id
            GROUP BY m.id, m.name, m.genres, m.directors, m.writers, m.cast, m.producers, m.release_year, m.age_rating, m.created_at
            ORDER BY m.created_at DESC
            """;
        return jdbcTemplate.query(sql, MOVIE_WITH_RATING_ROW_MAPPER);
    }

    public Movie save(Movie movie) {
        if (movie.getId() == null) {
            movie.setCreatedAt(LocalDateTime.now());
        }
        
        String sql = """
            INSERT INTO movies (id, name, genres, directors, writers, cast, producers, release_year, age_rating, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET 
            name = EXCLUDED.name,
            genres = EXCLUDED.genres,
            directors = EXCLUDED.directors,
            writers = EXCLUDED.writers,
            cast = EXCLUDED.cast,
            producers = EXCLUDED.producers,
            release_year = EXCLUDED.release_year,
            age_rating = EXCLUDED.age_rating
            """;
            
        jdbcTemplate.update(sql,
                movie.getId(),
                movie.getName(),
                movie.getGenres().toArray(new String[0]),
                movie.getDirectors().toArray(new String[0]),
                movie.getWriters().toArray(new String[0]),
                movie.getCast().toArray(new String[0]),
                movie.getProducers().toArray(new String[0]),
                movie.getReleaseYear(),
                movie.getAgeRating().name(),
                movie.getCreatedAt());
        
        return movie;
    }

    public boolean existsById(UUID id) {
        String sql = "SELECT COUNT(*) FROM movies WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}