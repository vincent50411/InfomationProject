package com.huntkey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

@EnableHystrix
@SpringBootApplication
public class EurekaHystrixDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaHystrixDemoApplication.class, args);
	}
}
