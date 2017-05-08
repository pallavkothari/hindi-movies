package com.pk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@SpringBootApplication
@Controller
public class MoviesApplication {

    @GetMapping("/")
    String home(Map<String, Object> model) {
        model.put("movies", new SfGateScraper().getMovies());
        return "index";
    }

	public static void main(String[] args) {
		SpringApplication.run(MoviesApplication.class, args);
	}
}
