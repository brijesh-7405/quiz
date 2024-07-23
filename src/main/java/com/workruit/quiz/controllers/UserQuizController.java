/**
 * 
 */
package com.workruit.quiz.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.ApiResponse;
import com.workruit.quiz.controllers.dto.Message;
import com.workruit.quiz.controllers.dto.PageApiResponse;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.controllers.dto.UserQuizDTO;
import com.workruit.quiz.controllers.dto.UserQuizDetailDTO;
import com.workruit.quiz.controllers.dto.UserQuizQuestionAnswerDTO;
import com.workruit.quiz.controllers.dto.UserQuizResponseDTO;
import com.workruit.quiz.controllers.dto.UserQuizStatusDTO;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.UserQuizService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Santosh
 *
 */
@Slf4j
@Controller
public class UserQuizController {

	private @Autowired UserQuizService userQuizService;
	private @Autowired MessageSource messageSource;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping(value = "/userquiz/quiz/{quizId}")
	public ResponseEntity create(@PathVariable("quizId") Long quizId, @RequestParam("code") String code) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			Long userQuizId = userQuizService.create(userDetailsDTO.getId(), quizId, code);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userQuizId);
			apiResponse.setMessage("User Quiz is created");
			apiResponse.setMsg(Message.builder()
					.description(
							messageSource.getMessage("user.quiz.create.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("user.quiz.create.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("user.quiz.create.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping("/userquiz/list")
	public ResponseEntity userQuizs(@RequestParam("page") int pageNumber, @RequestParam("size") int size) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			List<UserQuizDetailDTO> userQuizs = userQuizService.myQuizs(userDetailsDTO.getId(), pageNumber, size);
			PageApiResponse apiResponse = new PageApiResponse();
			apiResponse.setData(userQuizs);
			apiResponse.setNumberOfRecords(userQuizService.countByUserId(userDetailsDTO.getId()));
			apiResponse.setPage(pageNumber);
			apiResponse.setSize(size);
			apiResponse.setMessage("Quiz List Response");
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("user.quiz.list.success.description",
							new Object[] { pageNumber, size }, null))
					.title(messageSource.getMessage("user.quiz.list.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("user.quiz.list.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@PostMapping("/userquiz/{userquizid}/start")
	public ResponseEntity start(@PathVariable("userquizid") Long userQuizId, @RequestParam("page") int pageNumber,
			@RequestParam("size") int size) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			List<UserQuizQuestionAnswerDTO> questions = userQuizService.start(userDetailsDTO.getId(), userQuizId,
					pageNumber, size);
			PageApiResponse apiResponse = new PageApiResponse();
			apiResponse.setData(questions);
			apiResponse
					.setNumberOfRecords(userQuizService.countByUserIdAndUserquizId(userDetailsDTO.getId(), userQuizId));
			apiResponse.setPage(pageNumber);
			apiResponse.setSize(size);
			apiResponse.setMessage("User Quiz is started");
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("user.quiz.start.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("user.quiz.start.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("user.quiz.start.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping("/userquiz/update-answer")
	public ResponseEntity update(@RequestBody @Valid UserQuizDTO userQuizDTO) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			userQuizService.update(userQuizDTO.getQuestionId(), userQuizDTO.getUserQuizId(), userDetailsDTO.getId(),
					userQuizDTO.getAnswer());
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData("");
			apiResponse.setMessage("User Answer is saved");
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("user.quiz.updateanswer.success.description", new Object[] {},
							null))
					.title(messageSource.getMessage("user.quiz.updateanswer.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("user.quiz.updateanswer.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping("/userquiz/update-multiple-answers")
	public ResponseEntity update(@RequestBody @Valid List<UserQuizDTO> userQuizDTOs) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			for (UserQuizDTO userQuizDTO : userQuizDTOs) {
				userQuizService.update(userQuizDTO.getQuestionId(), userQuizDTO.getUserQuizId(), userDetailsDTO.getId(),
						userQuizDTO.getAnswer());
			}
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData("");
			apiResponse.setMessage("User Answer is saved");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping("/userquiz/{userquizid}/submit")
	public ResponseEntity submit(@PathVariable("userquizid") Long userQuizId) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			UserQuizResponseDTO userQuizResponseDTO = userQuizService.submit(userQuizId, userDetailsDTO.getId(),false);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userQuizResponseDTO);
			apiResponse.setMessage("User Quiz Submission Status");
			apiResponse.setMsg(Message.builder()
					.description(
							messageSource.getMessage("user.quiz.submit.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("user.quiz.submit.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("user.quiz.submit.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@GetMapping("/userquiz/{userquizid}/status")
	public ResponseEntity status(@PathVariable("userquizid") Long userQuizId) {
		try {

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			UserQuizStatusDTO userQuizStatusDTO = userQuizService.status(userDetailsDTO.getId(), userQuizId);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userQuizStatusDTO);
			apiResponse.setMessage("User Quiz Submission Status");
			apiResponse.setMsg(Message.builder()
					.description(
							messageSource.getMessage("user.quiz.status.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("user.quiz.status.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("user.quiz.status.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/userquiz/{userquizid}/question-status")
	public ResponseEntity quizQuestionsStatus(@PathVariable("userquizid") Long userQuizId) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userQuizService.getUserQuizQuestionsStatus(userQuizId, userDetailsDTO.getId()));
			apiResponse.setMessage("User Quiz Questions Status");
			apiResponse.setMsg(Message.builder().description("Question Status").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/userquiz/{userquizId}/question/{questionId}/answer")
	public ResponseEntity getUserQuizAnswerForQuestion(@PathVariable("userquizId") Long userQuizId,
			@PathVariable("questionId") Long questionId) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse
					.setData(userQuizService.getUserQuizQuestionAnswer(userQuizId, userDetailsDTO.getId(), questionId));
			apiResponse.setMessage("User Quiz Questions Status");
			apiResponse.setMsg(Message.builder().description("Answer").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/userquiz/timeleft/quiz/{quizId}")
	public ResponseEntity getUserQuizTimeLeft(@PathVariable("quizId") Long quizId) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userQuizService.getQuizTimeLeft(userDetailsDTO.getId(), quizId));
			apiResponse.setMessage("User Quiz timeleft");
			apiResponse.setMsg(Message.builder().description("User Quiz timeleft").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			log.error("Error while getting quic time limit",we);
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			log.error("Error while getting quic time limit",e);
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/userquiz/{userQuizId}/question/{questionId}/approveStatus/{approveStatus}")
	public ResponseEntity approveOrReject(@PathVariable("userQuizId") Long userQuizId,
			@PathVariable("questionId") Long questionId, @PathVariable("approveStatus") Boolean approvalStatus) {
		try {
			userQuizService.approveOrReject(userQuizId, questionId, approvalStatus);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

}
