/**
 * 
 */
package com.workruit.quiz.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Santosh
 *
 */
@Controller
public class HealthCheckController {

	@CrossOrigin(origins = "/*")
	@GetMapping("/health")
	public ResponseEntity health() {
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
