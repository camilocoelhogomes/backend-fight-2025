package com.backend.figth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FigthApplication {

	public static void main(String[] args) {
		System.out.println("Starting Figth Application");
		SpringApplication.run(FigthApplication.class, args);
	}

}
