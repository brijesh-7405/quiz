/**
 * 
 */
package com.workruit.quiz.controllers;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.ApiResponse;
import com.workruit.quiz.controllers.dto.Message;
import com.workruit.quiz.controllers.dto.PageApiResponse;
import com.workruit.quiz.controllers.dto.QuestionAnswerDTO;
import com.workruit.quiz.controllers.dto.QuestionAnswerMultipleDTO;
import com.workruit.quiz.controllers.dto.QuestionCreateResponseDTO;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.security.UserAuthorized;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.QuestionAnswerService;
import com.workruit.quiz.services.SubscriptionLimitService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Santosh
 *
 */
@Controller
@Slf4j
public class QuestionAnswerController {

	private @Autowired QuestionAnswerService questionAnswerService;
	private @Autowired SubscriptionLimitService subscriptionLimitService;

	@SuppressWarnings("rawtypes")
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@ResponseBody
	@GetMapping("/questionAndAnswer/quiz/{quizId}")
	public ResponseEntity getQuestions(@PathVariable("quizId") Long quizId, @RequestParam("page") int page,
			@RequestParam("size") int size) {
		try {
			PageApiResponse pageApiResponse = new PageApiResponse();
			pageApiResponse.setPage(page);
			pageApiResponse.setSize(size);
			pageApiResponse.setNumberOfRecords(questionAnswerService.countQuestions(quizId));
			pageApiResponse.setData(questionAnswerService.questions(quizId, page, size));
			pageApiResponse.setMsg(Message.builder().description("Get Question for Quiz").title("Success").build());
			pageApiResponse.setStatus("Success");
			return new ResponseEntity<>(pageApiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/questionAndAnswer")
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@ResponseBody
	public ResponseEntity create(@RequestBody @Valid QuestionAnswerDTO questionAnswerDTO) {
		try {
			QuestionCreateResponseDTO saveResponse = questionAnswerService.save(questionAnswerDTO, false);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(saveResponse);
			apiResponse.setMsg(Message.builder().description("Create Question").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (WorkruitException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/questions/check/{quizId}")
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@ResponseBody
	public ResponseEntity checkQuestionLimits(@PathVariable("quizId") Long quizId) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(subscriptionLimitService.checkQuestionsLimit(userDetailsDTO.getEnterpriseId(), quizId));
			apiResponse.setMsg(
					Message.builder().description("Check Question Limits").title("Subscription Limits").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (WorkruitException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/questionAndAnswer/{questionId}")
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@ResponseBody
	public ResponseEntity update(@RequestBody @Valid QuestionAnswerDTO questionAnswerDTO,
			@PathVariable("questionId") Long questionId) {
		try {
			questionAnswerService.update(questionAnswerDTO, questionId);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData("Updated question successfully");
			apiResponse.setMsg(Message.builder().description("Update Question").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (WorkruitException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/questionAndAnswerMultiple")
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@ResponseBody
	public ResponseEntity createMultiple(@RequestBody @Valid QuestionAnswerMultipleDTO questionAnswerMultipleDTO) {
		try {
			return new ResponseEntity(questionAnswerService.saveAll(questionAnswerMultipleDTO), HttpStatus.OK);
		} catch (WorkruitException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/questionAndAnswer/upload-image")
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@ResponseBody
	public ResponseEntity uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("quiz-id") Long quizId) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			String saveImage = questionAnswerService.saveImage(file, quizId);
			apiResponse.setData(saveImage);
			apiResponse.setMessage("Image uploaded");
			apiResponse.setMsg(Message.builder().description("Image uploaded").title("Success").build());
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/questionAndAnswer/upload-audio")
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@ResponseBody
	public ResponseEntity uploadAudio(@RequestParam("file") MultipartFile file, @RequestParam("quizId") Long quizId,
			@RequestParam("questionId") Long questionId) {
		try {
			String contentType = file.getContentType();
			if (contentType != null) {
				if (contentType.equalsIgnoreCase("audio/mpeg") || contentType.equalsIgnoreCase("audio/wave")
						|| contentType.equalsIgnoreCase("audio/ogg")) {
					ApiResponse apiResponse = new ApiResponse();
					String saveImage = questionAnswerService.saveAudio(file, quizId, questionId);
					apiResponse.setData(saveImage);
					apiResponse.setMessage("Audio uploaded");
					apiResponse.setMsg(Message.builder().description("Audio uploaded").title("Success").build());
					return new ResponseEntity<>(apiResponse, HttpStatus.OK);
				} else {
					throw new WorkruitException("Not a valid audio format");
				}
			} else {
				throw new WorkruitException("No Content Type");
			}
		} catch (WorkruitException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Exception while saving the audio", e);
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/questionAndAnswer/get-image")
	@ResponseBody
	public ResponseEntity getImage(@RequestParam("id") String id) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			String saveImage = questionAnswerService.getImage(id);
			apiResponse.setData(saveImage);
			apiResponse.setMessage("Image Retrieved");
			apiResponse.setMsg(Message.builder().description("Image Retrieved").title("Success").build());
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/questionAndAnswer/get-audio")
	@ResponseBody
	public ResponseEntity getAudio(@RequestParam("id") String id) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			String saveImage = questionAnswerService.getAudio(id);
			apiResponse.setData(saveImage);
			apiResponse.setMessage("Audio Retrieved");
			apiResponse.setMsg(Message.builder().description("Audio Retrieved").title("Success").build());
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}
}
