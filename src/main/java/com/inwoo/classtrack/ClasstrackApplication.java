package com.inwoo.classtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClasstrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClasstrackApplication.class, args);
	}

}
