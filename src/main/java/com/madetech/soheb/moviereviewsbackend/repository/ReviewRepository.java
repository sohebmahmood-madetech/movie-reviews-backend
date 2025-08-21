package com.madetech.soheb.moviereviewsbackend.repository;

import com.madetech.soheb.moviereviewsbackend.data.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Review> REVIEW_ROW_MAPPER = new RowMapper<Review>() {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            Review review = new Review();
            review.setId(UUID.fromString(rs.getString("id")));
            review.setMovieId(UUID.fromString(rs.getString("movie_id")));
            review.setUserId(UUID.fromString(rs.getString("user_id")));
            review.setRating(rs.getInt("rating"));
            review.setDescription(rs.getString("description"));
            review.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            return review;
        }
    };

    public Optional<Review> findById(UUID id) {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, REVIEW_ROW_MAPPER, id);
        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.get(0));
    }

    public List<Review> findByMovieId(UUID movieId) {
        String sql = "SELECT * FROM reviews WHERE movie_id = ? ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, REVIEW_ROW_MAPPER, movieId);
    }

    public List<Review> findByUserId(UUID userId) {
        String sql = "SELECT * FROM reviews WHERE user_id = ? ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, REVIEW_ROW_MAPPER, userId);
    }

    public Review save(Review review) {
        String sql = """
            INSERT INTO reviews (id, movie_id, user_id, rating, description, timestamp) 
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET 
            rating = EXCLUDED.rating,
            description = EXCLUDED.description,
            timestamp = EXCLUDED.timestamp
            """;
            
        jdbcTemplate.update(sql,
                review.getId(),
                review.getMovieId(),
                review.getUserId(),
                review.getRating(),
                review.getDescription(),
                review.getTimestamp());
        
        return review;
    }

    public boolean existsByUserIdAndMovieId(UUID userId, UUID movieId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND movie_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, movieId);
        return count != null && count > 0;
    }
}