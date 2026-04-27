package com.apiCodeGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.apiCodeGenerator")
public class ApiCodeGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiCodeGeneratorApplication.class, args);
	}

}
