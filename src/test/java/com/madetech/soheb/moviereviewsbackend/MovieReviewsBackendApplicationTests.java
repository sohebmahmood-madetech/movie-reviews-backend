package com.madetech.soheb.moviereviewsbackend;

import com.madetech.soheb.moviereviewsbackend.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@SpringBootTest
@ActiveProfiles("test")
class MovieReviewsBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
