package com.project.prototype;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/* @SpringBootApplication annotation to indicate that it is the
   main class of a Spring Boot application. The main method initializes and starts the Spring application. 
*/

@SpringBootApplication
public class SpringStarter {

	public static void main(String[] args) {
		SpringApplication.run(SpringStarter.class, args);
	}
}
