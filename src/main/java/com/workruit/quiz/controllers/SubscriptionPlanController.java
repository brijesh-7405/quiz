package com.workruit.quiz.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.workruit.quiz.controllers.dto.ApiResponse;
import com.workruit.quiz.controllers.dto.CustomSubscriptionPlanDTO;
import com.workruit.quiz.controllers.dto.Message;
import com.workruit.quiz.controllers.dto.PageApiResponse;
import com.workruit.quiz.controllers.dto.SubscriptionDetailsDTO;
import com.workruit.quiz.controllers.dto.SubscriptionPlanDTO;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.persistence.entity.PlanType;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.security.UserAuthorized;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.SubscriptionEnterprisePurchaseService;
import com.workruit.quiz.services.SubscriptionPlanFeatureMappingService;
import com.workruit.quiz.services.SubscriptionPlanService;

@Controller
public class SubscriptionPlanController {

	private @Autowired SubscriptionPlanService subscriptionPlanService;
	private @Autowired SubscriptionPlanFeatureMappingService subscriptionPlanFeatureMappingService;
	private @Autowired SubscriptionEnterprisePurchaseService subscriptionEnterprisePurchaseService;

	@UserAuthorized(userRoles = { UserRole.SUPERADMIN })
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping("/subscriptions")
	public ResponseEntity addSubscriptionPlan(@RequestBody @Validated SubscriptionPlanDTO subscriptionPlanDTO) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(subscriptionPlanService.addSubscriptionPlan(subscriptionPlanDTO));
			apiResponse.setMsg(
					Message.builder().description("Created subscription successfully").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@UserAuthorized(userRoles = { UserRole.SUPERADMIN })
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping("/subscriptions/custom")
	public ResponseEntity addCustomSubscriptionPlan(
			@RequestBody @Validated CustomSubscriptionPlanDTO customSubscriptionPlanDTO) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(subscriptionPlanService.addCustomSubscriptionPlan(customSubscriptionPlanDTO));
			apiResponse.setMsg(
					Message.builder().description("Created subscription successfully").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@UserAuthorized(userRoles = { UserRole.SUPERADMIN })
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping("/subscriptions/mapping")
	public ResponseEntity addSubscriptionPlanMapping(@RequestParam("subscriptionPlanId") Long subscriptionPlanId,
			@RequestParam("subscriptionFeatureId") Long subscriptionFeatureId) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(subscriptionPlanFeatureMappingService
					.addSubscriptionPlanFeatureMapping(subscriptionPlanId, subscriptionFeatureId));
			apiResponse.setMsg(Message.builder().description("Created subscription mapping successfully")
					.title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@GetMapping("/subscriptions/{subscriptionPlanId}")
	public ResponseEntity getSubscriptionPlan(@PathVariable("subscriptionPlanId") Long subscriptionPlanId) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(subscriptionPlanFeatureMappingService
					.getSubscriptionFeaturesBySubscriptionPlan(subscriptionPlanId));
			apiResponse.setMsg(Message.builder().description("Get subscription successfully").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER, UserRole.SUPERADMIN })
	@ResponseBody
	@GetMapping("/subscriptions")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ResponseEntity listSubscriptions(@RequestParam("page") int page, @RequestParam("size") int size,
			@RequestParam(value = "subscriptionName", required = false) String subscriptionName,
			@RequestParam(value = "planType", required = false) PlanType planType) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			PageApiResponse pageApiResponse = new PageApiResponse();
			if (userDetailsDTO.getEnterpriseId() != null) {
				if (subscriptionEnterprisePurchaseService.isCustomSubscription(userDetailsDTO.getEnterpriseId())) {
					List<SubscriptionDetailsDTO> list = new ArrayList<>();
					pageApiResponse.setStatus("Success");
					pageApiResponse.setPage(page);
					pageApiResponse.setSize(size);
					return new ResponseEntity<>(pageApiResponse, HttpStatus.OK);
				}
			}
			pageApiResponse.setData(subscriptionPlanService.listSubscriptions(page, size, subscriptionName, planType));
			pageApiResponse
					.setMsg(Message.builder().description("List subscription successfully").title("Success").build());
			pageApiResponse.setStatus("Success");
			pageApiResponse.setPage(page);
			pageApiResponse.setSize(size);
			pageApiResponse
					.setNumberOfRecords(subscriptionPlanService.getSubscriptionsCount(subscriptionName, planType));
			return new ResponseEntity(pageApiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}
}
