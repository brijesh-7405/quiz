/**
 * 
 */
package com.workruit.quiz.controllers;

import com.workruit.quiz.controllers.dto.Message;
import com.workruit.quiz.controllers.dto.PageApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.security.UserAuthorized;
import com.workruit.quiz.services.UserEnterpriseRequestService;

/**
 * @author Santosh Bhima
 *
 */
@RestController
public class UserEnterpriseRequestController {

	private @Autowired UserEnterpriseRequestService userEnterpriseRequestService;
	private @Autowired MessageSource messageSource;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@GetMapping("/enterprise/user/requests")
	public ResponseEntity getUserEnterpriseRequests(@RequestParam("page") int page, @RequestParam("size") int size) throws WorkruitException {
		UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Long enterpriseId = userDetailsDTO.getEnterpriseId();
		PageApiResponse pageApiResponse = new PageApiResponse();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		pageApiResponse.setNumberOfRecords(userEnterpriseRequestService.count(enterpriseId));
		pageApiResponse.setData(userEnterpriseRequestService.listUserRequests(enterpriseId, page, size));
		pageApiResponse.setSize(size);
		pageApiResponse.setMessage("User Enterprise Requests Data");
		pageApiResponse.setMsg(Message.builder()
				.description(messageSource.getMessage("user.enterprise.requests.list.success.description", new Object[] {}, null))
				.title(messageSource.getMessage("user.enterprise.requests.list.success.title", null, null)).build());
		pageApiResponse.setStatus(messageSource.getMessage("user.enterprise.requests.list.success.status", null, null));
		pageApiResponse.setPage(page);

		return new ResponseEntity(pageApiResponse, HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER })
	@PostMapping("/enterprise/user/requests/{userId}")
	public ResponseEntity updateUserEnterpriseStatus(@PathVariable("userId") Long userId,
			@RequestParam(value = "accept", defaultValue = "true") boolean accepted) throws WorkruitException {
		UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Long enterpriseId = userDetailsDTO.getEnterpriseId();
		userEnterpriseRequestService.updateUserStatus(enterpriseId, userId, accepted);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
