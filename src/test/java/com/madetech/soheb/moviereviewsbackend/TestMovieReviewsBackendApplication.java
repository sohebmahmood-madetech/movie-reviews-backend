package com.madetech.soheb.moviereviewsbackend;

import org.springframework.boot.SpringApplication;

public class TestMovieReviewsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(MovieReviewsBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
