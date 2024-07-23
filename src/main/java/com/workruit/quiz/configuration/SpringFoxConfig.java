/**
 * 
 */
package com.workruit.quiz.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Santosh
 *
 */
@Configuration
@EnableSwagger2
public class SpringFoxConfig implements WebMvcConfigurer {
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	@Bean
	public Docket apiDocket() {
		List<SecurityScheme> schemeList = new ArrayList<>();
		schemeList.add(new ApiKey(HttpHeaders.AUTHORIZATION, "Authorization", "header"));

		return new Docket(DocumentationType.SWAGGER_2).apiInfo(getApiInfo()).securitySchemes(schemeList)
				.securityContexts(Arrays.asList(securityContext())).select()
				.apis(RequestHandlerSelectors.basePackage("com.workruit.quiz.controllers")).paths(PathSelectors.any())
				.build();
	}

	@Bean
	SecurityConfiguration security() {
		return new SecurityConfiguration(null, null, null, null, null, ApiKeyVehicle.HEADER, "AUTHORIZATION", null);
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Arrays.asList(new SecurityReference("apiKey", authorizationScopes));
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.any()).build();
	}

	private ApiInfo getApiInfo() {
		return new ApiInfoBuilder().title("Swagger API Doc").description("More description about the API")
				.version("1.0.0").build();
	}
}