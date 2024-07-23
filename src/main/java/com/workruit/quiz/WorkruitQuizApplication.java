package com.workruit.quiz;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class WorkruitQuizApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkruitQuizApplication.class, args);
	}

	//@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("http://localhost:5000");
				registry.addMapping("/**").allowedMethods("*");
				registry.addMapping("/**").allowedHeaders("*");
			}
		};
	}
	
	@Bean
	public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
	    ByteArrayHttpMessageConverter arrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
	    arrayHttpMessageConverter.setSupportedMediaTypes(getSupportedMediaTypes());
	    return arrayHttpMessageConverter;
	}

	private List<MediaType> getSupportedMediaTypes() {
	    List<MediaType> list = new ArrayList<MediaType>();
	    list.add(MediaType.IMAGE_JPEG);
	    list.add(MediaType.IMAGE_PNG);
	    list.add(MediaType.APPLICATION_OCTET_STREAM);
	    return list;
	}
}
