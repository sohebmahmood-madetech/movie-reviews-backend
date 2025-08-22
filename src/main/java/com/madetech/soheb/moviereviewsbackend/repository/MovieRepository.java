package com.madetech.soheb.moviereviewsbackend.repository;

import com.madetech.soheb.moviereviewsbackend.data.database.Movie;
import com.madetech.soheb.moviereviewsbackend.data.controller.MovieWithRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    @Query("""
        SELECT m, AVG(CAST(r.rating AS double))
        FROM Movie m LEFT JOIN Review r ON m.id = r.movie.id
        GROUP BY m.id
        ORDER BY m.createdAt DESC
        """)
    List<Object[]> findAllMoviesWithAverageRating();
}