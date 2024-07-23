/**
 * 
 */
package com.workruit.quiz.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.workruit.quiz.configuration.WorkruitAuthorizationException;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.ApiResponse;
import com.workruit.quiz.controllers.dto.Message;
import com.workruit.quiz.controllers.dto.PageApiResponse;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.controllers.dto.UserQuestionAnalyticsDataDTO.AnswerStatus;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;
import com.workruit.quiz.security.UserAuthorized;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.QuestionAnalyticsService;
import com.workruit.quiz.services.QuizAnalyticsService;
import com.workruit.quiz.services.SubscriptionLimitService;
import com.workruit.quiz.services.UserQuizAnalyticsService;
import com.workruit.quiz.services.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Santosh
 *
 */
@Controller
@Slf4j
public class AnalyticsController {

	private @Autowired QuizAnalyticsService quizAnalyticsService;
	private @Autowired QuestionAnalyticsService questionAnalyticsService;
	private @Autowired UserQuizAnalyticsService userQuizAnalyticsService;
	private @Autowired UserService UserService;
	private @Autowired MessageSource messageSource;
	private @Autowired SubscriptionLimitService subscriptionLimitService;

	// This is 11G from requirements
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/analytics/quiz")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	public ResponseEntity analytics(@RequestParam("page") int page, @RequestParam("size") int size,
			@RequestParam(value = "sortBy", defaultValue = "enterpriseName:ASC") String sortBy) {
		try {
			PageApiResponse pageApiResponse = new PageApiResponse();
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			pageApiResponse.setNumberOfRecords(quizAnalyticsService.count(userDetailsDTO.getEmail()));
			pageApiResponse.setData(quizAnalyticsService.analyticsData(page, size, sortBy, userDetailsDTO.getEmail()));
			pageApiResponse.setSize(size);
			pageApiResponse.setMessage("Quiz Analytics Data");
			pageApiResponse.setPage(page);
			pageApiResponse.setMsg(Message.builder()
					.description(
							messageSource.getMessage("analytics.quiz.list.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("analytics.quiz.list.success.title", null, null)).build());
			pageApiResponse.setStatus(messageSource.getMessage("analytics.quiz.list.success.status", null, null));
			return new ResponseEntity(pageApiResponse, HttpStatus.OK);
		} catch (WorkruitAuthorizationException we) {
			String description = "User doesnt have sufficient permission";
			return ControllerUtils.customErrorMessage(description);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	/// Need to check status once expiry date is done 
	// This is 12G from requirements
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/analytics/quiz/{quizId}")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	public ResponseEntity analyticsByQuiz(@PathVariable("quizId") Long quizId) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();

			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(quizAnalyticsService.analyticsQuizData(quizId, userDetailsDTO.getEnterpriseId()));
			apiResponse.setMessage("Analytics Data by Quiz Id");
			apiResponse.setMsg(Message.builder()
					.description(
							messageSource.getMessage("analytics.quiz.get.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("analytics.quiz.get.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("analytics.quiz.get.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (WorkruitAuthorizationException we) {
			return ControllerUtils.customErrorMessage("User doesnt have sufficient permission");
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/analytics/enterprise/{enterpriseId}")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.SUPERADMIN })
	public ResponseEntity analyticsByEnterprise(@PathVariable("enterpriseId") Long enterpriseId,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		try {
			PageApiResponse apiResponse = new PageApiResponse();
			apiResponse.setData(quizAnalyticsService.analyticsEnterpriseQuizData(enterpriseId, page, size));
			apiResponse.setNumberOfRecords(quizAnalyticsService.countByEnterpriseId(enterpriseId));
			apiResponse.setSize(size);
			apiResponse.setPage(page);
			apiResponse.setMessage("Analytics Data by Enterprise Id");
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("analytics.enterprise.get.success.description",
							new Object[] {}, null))
					.title(messageSource.getMessage("analytics.enterprise.get.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("analytics.enterprise.get.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	// This is 13G from requirements document.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/analytics/quiz/{quizId}/questions")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	public ResponseEntity analyticsByQuestions(@PathVariable("quizId") Long quizId, @RequestParam("page") int page,
			@RequestParam("size") int size) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();

			PageApiResponse pageApiResponse = new PageApiResponse();
			pageApiResponse
					.setNumberOfRecords(questionAnalyticsService.count(quizId, userDetailsDTO.getEnterpriseId()));
			pageApiResponse.setData(questionAnalyticsService.analyticsQuestionsData(quizId, page, size));
			pageApiResponse.setSize(size);
			pageApiResponse.setMessage("Quiz Analytics Data");
			pageApiResponse.setPage(page);
			pageApiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("analytics.quiz.questions.success.description",
							new Object[] {}, null))
					.title(messageSource.getMessage("analytics.quiz.questions.success.title", null, null)).build());
			pageApiResponse.setStatus(messageSource.getMessage("analytics.quiz.questions.success.status", null, null));
			return new ResponseEntity(pageApiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/analytics/quiz/{quizId}/questions/{questionId}/status/{status}")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	public ResponseEntity analyticsByQuestionsQuestionStatus(@PathVariable("quizId") Long quizId,
			@PathVariable("questionId") Long questionId, @PathVariable("status") AnswerStatus status,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			PageApiResponse pageApiResponse = new PageApiResponse();
			pageApiResponse.setNumberOfRecords(questionAnalyticsService.questionAnalyticsCount(quizId, questionId,
					status, userDetailsDTO.getEnterpriseId()));
			pageApiResponse.setData(
					questionAnalyticsService.questionAnalyticsByPagination(quizId, questionId, status, page, size));
			pageApiResponse.setSize(size);
			pageApiResponse.setMessage("Quiz Analytics Data");
			pageApiResponse.setPage(page);
			pageApiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("analytics.quiz.questions.question.success.description",
							new Object[] {}, null))
					.title(messageSource.getMessage("analytics.quiz.questions.question.success.title", null, null))
					.build());
			pageApiResponse.setStatus(
					messageSource.getMessage("analytics.quiz.questions.question.success.status", null, null));
			return new ResponseEntity(pageApiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/analytics/userquiz/quiz/{quizId}")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	public ResponseEntity analyticsByUserQuiz(@PathVariable("quizId") Long quizId, @RequestParam("page") int page,
			@RequestParam("size") int size) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			subscriptionLimitService.checkApplicantsLimit(userDetailsDTO.getEnterpriseId(), page * size);
			PageApiResponse pageApiResponse = new PageApiResponse();
			pageApiResponse
					.setNumberOfRecords(userQuizAnalyticsService.count(quizId, userDetailsDTO.getEnterpriseId()));
			pageApiResponse
					.setData(userQuizAnalyticsService.list(page, size, quizId, userDetailsDTO.getEnterpriseId()));
			pageApiResponse.setSize(size);
			pageApiResponse.setMessage("Quiz Analytics Data");
			pageApiResponse.setPage(page);
			pageApiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("analytics.userquiz.quiz.get.success.description",
							new Object[] {}, null))
					.title(messageSource.getMessage("analytics.userquiz.quiz.get.success.title", null, null)).build());
			pageApiResponse
					.setStatus(messageSource.getMessage("analytics.userquiz.quiz.get.success.status", null, null));
			return new ResponseEntity(pageApiResponse, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception while getting user quiz id", e);
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/analytics/users/{userId}")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER, UserRole.SUPERADMIN })
	public ResponseEntity analyticsByUserId(@PathVariable("userId") Long userId, @RequestParam("page") int page,
			@RequestParam("size") int size) {
		try {
			PageApiResponse pageApiResponse = new PageApiResponse();
			pageApiResponse.setNumberOfRecords(userQuizAnalyticsService.countByUserId(userId));
			pageApiResponse.setData(userQuizAnalyticsService.listByUserId(page, size, userId));
			pageApiResponse.setSize(size);
			pageApiResponse.setMessage("Quiz Analytics Data");
			pageApiResponse.setPage(page);
			pageApiResponse.setMsg(Message.builder()
					.description(
							messageSource.getMessage("analytics.user.get.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("analytics.user.get.success.title", null, null)).build());
			pageApiResponse.setStatus(messageSource.getMessage("analytics.user.get.success.status", null, null));
			return new ResponseEntity(pageApiResponse, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception while getting user id", e);
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/analytics/userquiz/quiz/{quizId}/{status}")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	public ResponseEntity analyticsByUserQuizAndStatus(@PathVariable("quizId") Long quizId,
			@RequestParam("page") int page, @RequestParam("size") int size,
			@PathVariable("status") UserQuizStatus status,
			@RequestParam(value = "autoCompleted", required = false) boolean autoCompleted,
			@RequestParam(value = "all", required = false) boolean all) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			subscriptionLimitService.checkApplicantsLimit(userDetailsDTO.getEnterpriseId(), page * size);
			PageApiResponse pageApiResponse = new PageApiResponse();
			if (status != UserQuizStatus.COMPLETED) {
				pageApiResponse.setNumberOfRecords(
							userQuizAnalyticsService.countByQuizIdAndAutoCompleted(status, quizId, userDetailsDTO.getEnterpriseId(),autoCompleted));
				pageApiResponse.setData(userQuizAnalyticsService.listByStatus(status, page, size, quizId,
						userDetailsDTO.getEnterpriseId(), autoCompleted));
			} else {
				if (!all) {
					pageApiResponse.setNumberOfRecords(userQuizAnalyticsService.countByQuizIdAndAutoCompleted(status,
							quizId, userDetailsDTO.getEnterpriseId(), autoCompleted));
					pageApiResponse.setData(userQuizAnalyticsService.listByStatus(status, page, size, quizId,
							userDetailsDTO.getEnterpriseId(), autoCompleted));
				} else {
					pageApiResponse.setNumberOfRecords(userQuizAnalyticsService
							.countByStatusAndQuizIdAndEnterpriseId(status, quizId, userDetailsDTO.getEnterpriseId()));
					pageApiResponse.setData(userQuizAnalyticsService.listByStatusWithoutAutoComplete(status, page, size,
							quizId, userDetailsDTO.getEnterpriseId()));
				}

			}
			pageApiResponse.setSize(size);
			pageApiResponse.setMessage("Quiz Analytics Data");
			pageApiResponse.setPage(page);
			pageApiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("analytics.userquiz.quiz.status.success.description",
							new Object[] {}, null))
					.title(messageSource.getMessage("analytics.userquiz.quiz.status.success.title", null, null))
					.build());
			pageApiResponse
					.setStatus(messageSource.getMessage("analytics.userquiz.quiz.status.success.status", null, null));
			return new ResponseEntity(pageApiResponse, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception while getting user quiz stats", e);
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/analytics/userquiz/quiz/{quizId}/stats")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	public ResponseEntity analyticsByUserQuizAndStats(@PathVariable("quizId") Long quizId) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userQuizAnalyticsService.getStats(quizId, userDetailsDTO.getEnterpriseId()));
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("analytics.userquiz.quiz.stats.success.description",
							new Object[] {}, null))
					.title(messageSource.getMessage("analytics.userquiz.quiz.stats.success.title", null, null))
					.build());
			apiResponse.setStatus(messageSource.getMessage("analytics.userquiz.quiz.stats.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			log.error("Exception while getting quizstats", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/analytics/users")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.SUPERADMIN })
	public ResponseEntity analyticsByUsers(@RequestParam("page") int page, @RequestParam("size") int size) {
		try {
			PageApiResponse pageApiResponse = new PageApiResponse();
			pageApiResponse.setNumberOfRecords(UserService.count());
			pageApiResponse.setData(UserService.getUsers(page, size));
			pageApiResponse.setSize(size);
			pageApiResponse.setMessage("User Data");
			pageApiResponse.setPage(page);
			pageApiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("analytics.users.success.description", new Object[] {}, null))
					.title(messageSource.getMessage("analytics.users.success.title", null, null)).build());
			pageApiResponse.setStatus(messageSource.getMessage("analytics.users.success.status", null, null));
			return new ResponseEntity(pageApiResponse, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception while getting user analytics", e);
			return ControllerUtils.genericErrorMessage();
		}
	}
}
