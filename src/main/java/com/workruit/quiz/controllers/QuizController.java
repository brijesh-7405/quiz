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
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.workruit.quiz.controllers.dto.QuizDTO;
import com.workruit.quiz.controllers.dto.QuizQuestionComments;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.controllers.dto.UserQuizDetailDTO;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.security.UserAuthorized;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.QuizService;
import com.workruit.quiz.services.SubscriptionLimitService;
import com.workruit.quiz.services.UserQuizService;

/**
 * @author Santosh
 *
 */
@Controller
public class QuizController {

	private @Autowired QuizService quizService;
	private @Autowired UserQuizService userQuizService;
	private @Autowired MessageSource messageSource;
	private @Autowired SubscriptionLimitService subscriptionLimitService;

	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/quiz")
	@ResponseBody
	public ResponseEntity create(@RequestBody @Valid QuizDTO quizDTO) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			long quizId = quizService.createQuiz(quizDTO, userDetailsDTO.getEnterpriseId());
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(quizId);
			apiResponse.setMessage("Quiz created successfully");
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("quiz.create.success.description",
							new Object[] { quizDTO.getName() }, null))
					.title(messageSource.getMessage("quiz.create.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("quiz.create.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.customErrorMessage(e.getMessage());
		}
	}

	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/quiz/check")
	@ResponseBody
	public ResponseEntity checkForQuizCreate() {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(subscriptionLimitService.checkQuizLimit(userDetailsDTO.getEnterpriseId()));
			apiResponse.setMessage("Quiz created successfully");
			apiResponse.setMsg(Message.builder().description("Checking for quiz creation limits")
					.title("Subscription Limits").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/quiz/{id}")
	@ResponseBody
	public ResponseEntity update(@RequestBody @Valid QuizDTO quizDTO, @PathVariable("id") Long id) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage("Updated quiz succesfully");
			quizService.updateQuiz(quizDTO, id);
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("quiz.update.success.description",
							new Object[] { quizDTO.getName() }, null))
					.title(messageSource.getMessage("quiz.update.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("quiz.update.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch(WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@DeleteMapping("/quiz/{id}")
	@ResponseBody
	public ResponseEntity delete(@PathVariable("id") Long id) {
		try {
			quizService.deleteQuiz(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/quiz/{quiz_id}/status/{status}")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.SUPERADMIN, UserRole.ENTERPRISE_USER })
	public ResponseEntity updateQuizStatus(@PathVariable("quiz_id") Long quizId,
			@PathVariable("status") QuizSubmitStatus status,
			@RequestBody(required = false) QuizQuestionComments comments) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			if (status == QuizSubmitStatus.REVIEW
					&& userDetailsDTO.getUserRole().equals(UserRole.ENTERPRISE_USER.name())) {
				quizService.updateQuizStatus(quizId, status);
			} else if ((status == QuizSubmitStatus.REJECT || status == QuizSubmitStatus.ACTIVE)
					&& userDetailsDTO.getUserRole().equals(UserRole.SUPERADMIN.name())) {
				quizService.updateQuizStatus(quizId, status);
				if (status == QuizSubmitStatus.REJECT) {
					quizService.updateQuizComments(quizId, comments);
				}
			} else if (status == QuizSubmitStatus.CLOSE) {
				quizService.updateQuizStatus(quizId, status);
			} else {
				throw new WorkruitException("UnAuthorized access to Quiz");
			}
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData("");
			apiResponse.setMessage("Quiz status updated");
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("quiz.status.success.description",
							new Object[] { status.name(), quizId }, null))
					.title(messageSource.getMessage("quiz.status.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("quiz.status.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/quiz/top")
	@ResponseBody
	public ResponseEntity topQuizzes() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userQuizService.topQuizs(userDetailsDTO.getId()));
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("quiz.top.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("quiz.top.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("quiz.top.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@GetMapping("/quiz/{quiz_id}")
	@ResponseBody
	public ResponseEntity getQuiz(@PathVariable("quiz_id") Long quizId) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			List<UserQuizDetailDTO> myQuiz = userQuizService.myQuiz(userDetailsDTO.getId(), quizId);
			apiResponse.setData(myQuiz);
			apiResponse.setMsg(Message.builder()
					.description(
							messageSource.getMessage("quiz.get.success.description", new Object[] { quizId }, null))
					.title(messageSource.getMessage("quiz.get.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("quiz.get.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@ResponseBody
	@GetMapping("/quiz/{quiz_id}/preview")
	public ResponseEntity getQuizPreview(@PathVariable("quiz_id") Long quizId) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(quizService.quizPreview(quizId));
			apiResponse.setMessage("Quiz preview response");
			apiResponse.setMsg(Message.builder()
					.description(
							messageSource.getMessage("quiz.preview.success.description", new Object[] { quizId }, null))
					.title(messageSource.getMessage("quiz.preview.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("quiz.preview.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@GetMapping("/quiz/list")
	public ResponseEntity listQuiz(@RequestParam("page") int page, @RequestParam("size") int size) {
		try {
			PageApiResponse pageApiResponse = userQuizService.listQuizs(page, size);
			pageApiResponse.setMsg(Message.builder().description("List Quizzes").title("Success").build());
			pageApiResponse.setStatus("Success");
			return new ResponseEntity<>(pageApiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping(path = { "/quiz/filter/enterprise/{enterpriseId}/categories/{categoryIds}",
			"/quiz/filter/enterprise//categories/{categoryIds}", "/quiz/filter/enterprise//categories/",
			"/quiz/filter/enterprise/{enterpriseId}/categories/" })
	public ResponseEntity filter(@PathVariable(value = "enterpriseId", required = false) Long enterpriseId,
			@PathVariable(value = "categoryIds", required = false) List<Long> categoryIds) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userQuizService.filter(userDetailsDTO.getId(), enterpriseId, categoryIds));
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("quiz.filter.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("quiz.filter.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("quiz.filter.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping(path = { "/quiz/filterByPagination/enterprise/{enterpriseId}/categories/{categoryIds}",
			"/quiz/filterByPagination/enterprise//categories/{categoryIds}",
			"/quiz/filterByPagination/enterprise//categories/",
			"/quiz/filterByPagination/enterprise/{enterpriseId}/categories/" })
	public ResponseEntity filterByPagination(@PathVariable(value = "enterpriseId", required = false) Long enterpriseId,
			@PathVariable(value = "categoryIds", required = false) List<Long> categoryIds,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			PageApiResponse apiResponse = userQuizService.filterWithPagination(enterpriseId, categoryIds, page, size,
					userDetailsDTO.getId());
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("quiz.filter.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("quiz.filter.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("quiz.filter.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}
}
