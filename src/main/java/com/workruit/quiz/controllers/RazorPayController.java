/**
 * 
 */
package com.workruit.quiz.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.workruit.quiz.controllers.dto.PageApiResponse;
import com.workruit.quiz.controllers.dto.PurchaseOrderResponse;
import com.workruit.quiz.controllers.dto.PurchaseRequest;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.security.UserAuthorized;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.RazorPayService;

/**
 * @author Santosh Bhima
 *
 */
@Controller
public class RazorPayController {
	private @Autowired RazorPayService razorPayService;
	private @Value("${ui.baseurl}") String uiUrl;

	@PostMapping("/payment/callback")
	public ResponseEntity paymentCallback(@RequestParam("razorpay_order_id") String razorPayOrderId,
			@RequestParam("razorpay_signature") String signature, @RequestParam("razorpay_payment_id") String paymentId,
			@RequestParam("transaction_id") String transactionId) {
		boolean result = razorPayService.confirmOrder(paymentId, signature, razorPayOrderId, transactionId, false);
		return new ResponseEntity(HttpStatus.OK);
	}

	@PostMapping("/payment/callback/auto_renew")
	public ResponseEntity paymentCallbackAutorenew(@RequestParam("razorpay_subscription_id") String razorPayOrderId,
			@RequestParam("razorpay_signature") String signature, @RequestParam("razorpay_payment_id") String paymentId,
			@RequestParam("transaction_id") String transactionId) {
		razorPayService.confirmOrder(paymentId, signature, razorPayOrderId, transactionId, true);
		return new ResponseEntity(HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@UserAuthorized(userRoles = UserRole.ENTERPRISE_USER)
	@ResponseBody
	@PostMapping("/payment/order")
	public ResponseEntity createPurchaseOrder(@RequestBody PurchaseRequest purchaseRequest,
			@RequestParam("auto_renew") boolean autoRenew) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			Long enterpriseId = userDetailsDTO.getEnterpriseId();
			PurchaseOrderResponse createPurchaseOrder = razorPayService.createPurchaseOrder(purchaseRequest,
					enterpriseId, userDetailsDTO.getId(), autoRenew);
			return new ResponseEntity<>(createPurchaseOrder, HttpStatus.CREATED);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@GetMapping("/payment/generatepaymentlink")
	public ResponseEntity generatePurchaseLink(@RequestParam("subscription_plan_id") Long planId,
			@RequestParam("enterprise_id") Long enterpriseId) {
		try {
			return new ResponseEntity<>("https://quiz.workruit.com/payment/custom_order?subscription_plan_id=" + planId
					+ "&enterprise_id=" + enterpriseId, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@GetMapping("/payment/custom_order")
	public ResponseEntity createPurchaseOrder(@RequestParam("subscription_plan_id") Long planId,
			@RequestParam("enterprise_id") Long enterpriseId) {
		try {
			PurchaseRequest purchaseRequest = new PurchaseRequest();
			purchaseRequest.setSubscriptionPlanId(planId);
			PurchaseOrderResponse createPurchaseOrder = razorPayService.createPurchaseOrder(purchaseRequest,
					enterpriseId, -1L, true);
			return new ResponseEntity<>(createPurchaseOrder, HttpStatus.CREATED);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@UserAuthorized(userRoles = UserRole.ENTERPRISE_USER)
	@ResponseBody
	@GetMapping("/payment/orderConfirmation/{orderId}")
	public ResponseEntity getOrderDetails(@PathVariable("orderId") String orderId) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			Long enterpriseId = userDetailsDTO.getEnterpriseId();
			return new ResponseEntity<>(razorPayService.getOrderDetails(orderId, enterpriseId), HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/payment/autorenew/confirmation")
	public ResponseEntity confirmAutoRenewPayment(@RequestBody String body,
			@RequestHeader("X-Razorpay-Signature") String signature)
			throws JsonMappingException, JsonProcessingException {
		razorPayService.validateAutoRenew("abcd", body, signature);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@SuppressWarnings("rawtypes")
	@UserAuthorized(userRoles = UserRole.ENTERPRISE_USER)
	@ResponseBody
	@GetMapping("/payment/invoices/{invoice_number}")
	public ResponseEntity getInvoice(@PathVariable("invoice_number") String invoiceNumber) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			Long enterpriseId = userDetailsDTO.getEnterpriseId();
			return new ResponseEntity<>(razorPayService.getInvoice(invoiceNumber, enterpriseId), HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@UserAuthorized(userRoles = UserRole.ENTERPRISE_USER)
	@ResponseBody
	@GetMapping("/payment/invoices")
	public ResponseEntity getInvoices(@RequestParam("pageNumber") int pageNumber,
			@RequestParam("pageSize") int pageSize) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			Long enterpriseId = userDetailsDTO.getEnterpriseId();
			PageApiResponse pageApiResponse = new PageApiResponse();
			pageApiResponse.setData(razorPayService.getInvoices(enterpriseId, pageNumber, pageSize));
			pageApiResponse.setPage(pageNumber);
			pageApiResponse.setSize(pageSize);
			pageApiResponse.setNumberOfRecords(razorPayService.count(enterpriseId));
			return new ResponseEntity<>(pageApiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

}
