package com.techeazy.aws.batch2.restcontroller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloWorld {

	@GetMapping("/hello")
	public String sayHello() {
		return "Hello from Spring Boot!";
	}
}