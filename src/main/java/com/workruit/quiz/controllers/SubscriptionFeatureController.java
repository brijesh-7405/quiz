/**
 * 
 */
package com.workruit.quiz.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.workruit.quiz.controllers.dto.ApiResponse;
import com.workruit.quiz.controllers.dto.Message;
import com.workruit.quiz.controllers.dto.SubscriptionFeatureDTO;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.SubscriptionFeatureService;

/**
 * @author Santosh Bhima
 *
 */
@Controller
public class SubscriptionFeatureController {

	private @Autowired SubscriptionFeatureService subscriptionFeatureService;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping("/subscriptions/features/")
	public ResponseEntity addSubscriptionFeature(
			@RequestBody @Validated SubscriptionFeatureDTO subscriptionFeatureDTO) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(subscriptionFeatureService.addSubscriptionFeature(subscriptionFeatureDTO));
			apiResponse.setMsg(Message.builder().description("Creating Subscription Feature").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}
}
