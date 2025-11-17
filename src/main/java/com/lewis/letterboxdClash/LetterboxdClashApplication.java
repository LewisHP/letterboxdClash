package com.lewis.letterboxdClash;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LetterboxdClashApplication {

	public static void main(String[] args) {
		SpringApplication.run(LetterboxdClashApplication.class, args);
	}

	@Bean
	CommandLineRunner logEnvironmentVariables() {
		return args -> {
			System.out.println("=== ENVIRONMENT VARIABLES ===");
			System.getenv().forEach((key, value) -> {
				// Mask sensitive values but show if they exist
				if (key.contains("KEY") || key.contains("SECRET") || key.contains("PASSWORD")) {
					System.out.println(key + " = [SET - length: " + value.length() + "]");
				} else {
					System.out.println(key + " = " + value);
				}
			});
			System.out.println("=== END ENVIRONMENT VARIABLES ===");
		};
	}

}
