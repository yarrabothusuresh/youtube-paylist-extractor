package com.example.youtubeextractor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI youTubeExtractorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("YouTube Playlist Video Extractor API")
                        .description("REST API to extract all video URLs from YouTube playlists with pagination support.")
                        .version("1.0.0")
                        .contact(new Contact().name("YouTube Extractor Team")));
    }
}
