/**
 * 
 */
package com.workruit.quiz.controllers;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.workruit.quiz.controllers.dto.*;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Version;
import com.restfb.scope.ScopeBuilder;
import com.workruit.quiz.configuration.ConflictException;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.persistence.entity.repository.UserVerificationRepository;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.UserService;

/**
 * @author Santosh
 *
 */
@Controller
public class UserController {
	private @Autowired UserService userService;
	private @Autowired UserVerificationRepository userVerificationRepository;

	@Value("${application.url:test}")
	private String applicationUrl;

	@Value("${env.type:dev}")
	private String envType;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/get-sms-code")
	@ResponseBody
	public ResponseEntity dummy(@RequestParam("userid") Long id) {
		return new ResponseEntity(userService.getOTP(id), HttpStatus.OK);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/signup")
	@ResponseBody
	public ResponseEntity save(@RequestBody @Valid UserSignupProfileDTO userProfileDTO) {
		try {
			long id = userService.save(userProfileDTO);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(id);
			if (envType.equals("dev")) {
				apiResponse
						.setMessage("User Signup Successful:" + userVerificationRepository.findByUserId(id).getOtp());
			} else {
				apiResponse.setMessage("User Signup Successful");
			}
			apiResponse.setMsg(Message.builder().description("User Signup Successful").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (ConflictException e) {
			return ControllerUtils.customErrorMessage(e.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/signup/social")
	@ResponseBody
	public ResponseEntity saveSocial(@RequestBody @Valid UserSocialSignupProfileDTO userProfileDTO) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userService.saveSocial(userProfileDTO));
			apiResponse.setMsg(Message.builder().description("User Signup Successful").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/verify/{user_id}")
	@ResponseBody
	public ResponseEntity verifyUser(@PathVariable("user_id") Long userId,
			@RequestParam("token") @Size(min = 6, max = 6, message = "Token length should be equal to six characters") String token) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userService.verifyUser(userId, token));
			apiResponse.setMsg(Message.builder().description("Verify User").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage("Wrong OTP");
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@PostMapping("/sendOTP")
	@ResponseBody
	public ResponseEntity sendOTP(@RequestParam("userId") Long id) {
		try {
			userService.sendOTP(id);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg(Message.builder().description("Send OTP Success").title("Success").build());
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@PostMapping("/sendOTPForLogin")
	@ResponseBody
	public ResponseEntity sendOTP(@RequestParam("mobile") @Size(min = 10, max = 10, message = "") String mobile) {
		try {
			Map<String, Object> sendOTPforLogin = userService.sendOTPforLogin(mobile);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(sendOTPforLogin);
			apiResponse.setMsg(Message.builder().description("Sent OTP for Login").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}
	
	@SuppressWarnings({ "rawtypes" })
	@PostMapping("/validateUser")
	@ResponseBody
	public ResponseEntity validateUser(@RequestParam("mobile") @Size(min = 10, max = 10, message = "") String mobile) {
		try {
			Map<String, Object> sendOTPforLogin = userService.validateUser(mobile);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(sendOTPforLogin);
			apiResponse.setMsg(Message.builder().description("Validate User").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@GetMapping("/signup-facebook")
	public String facebookLogin(@RequestParam("app_identifier") String appIdentifier) {
		DefaultFacebookClient facebookClient = new DefaultFacebookClient(Version.LATEST);
		String loginDialogUrl = facebookClient.getLoginDialogUrl("273388349515880",
				applicationUrl + "/facebook/callback", new ScopeBuilder());
		return "redirect:" + loginDialogUrl + "&state=" + appIdentifier;
	}

	@GetMapping("/facebook/callback")
	public void facebookCallback(@RequestParam("code") String code, @RequestParam("state") String appIdentifier) {
		DefaultFacebookClient facebookClient = new DefaultFacebookClient(Version.LATEST);
		String loginDialogUrl = facebookClient.getLoginDialogUrl("273388349515880",
				applicationUrl + "/facebook/callback?appIdentifier=" + appIdentifier, new ScopeBuilder());
		AccessToken accessToken = facebookClient.obtainUserAccessToken("273388349515880",
				"b2903496b00f7f95455f1e74decfd996", loginDialogUrl, code);
		System.out.println(accessToken.getAccessToken());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PutMapping("/profile")
	@ResponseBody
	public ResponseEntity update(@RequestBody @Valid UserProfileDTO userProfileDTO) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			userProfileDTO.setId(userDetailsDTO.getId());
			userService.update(userProfileDTO);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage("User updated Successful");
			apiResponse.setData("");
			apiResponse.setMsg(Message.builder().description("User updated Successful").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("/addCode")
	@ResponseBody
	public ResponseEntity addCode(@RequestParam("code") String code) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			userService.addCode(code, userDetailsDTO.getId());
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage("User code added Successfully");
			apiResponse.setData("");
			apiResponse.setMsg(Message.builder().description("User code added Successful").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/getCodes")
	@ResponseBody
	public ResponseEntity getCodes() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage("User code get Successfully");
			apiResponse.setData(userService.getCodeDetails(userDetailsDTO.getId()));
			apiResponse.setMsg(Message.builder().description("User code get Successful").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@GetMapping("/profile")
	public ResponseEntity profile() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			UserProfileDTO user = userService.get(userDetailsDTO.getId());
			if (user == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setMsg(Message.builder().description("User Profile").title("Success").build());
				apiResponse.setStatus("Success");
				apiResponse.setData(user);
				return new ResponseEntity(apiResponse, HttpStatus.OK);
			}
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PutMapping("/update-detail")
	@ResponseBody
	public ResponseEntity updateDetail(@RequestBody @Valid UserInterestsDTO userInterestsDTO) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			userService.profileUpdate(userInterestsDTO, userDetailsDTO.getId());
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage("User updated Successful");
			apiResponse.setMsg(Message.builder().description("User updated Successful").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@GetMapping("/profile-detail")
	public ResponseEntity profileDetail() {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			UserInterestsDTO userInterestsDTO = userService.profileDetails(userDetailsDTO.getId());
			if (userInterestsDTO == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} else {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setMsg(Message.builder().description("User details").title("Success").build());
				apiResponse.setStatus("Success");
				apiResponse.setData(userInterestsDTO);
				return new ResponseEntity(apiResponse, HttpStatus.OK);
			}
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@ResponseBody
	@PostMapping("/update-password")
	public ResponseEntity updatePassword(@RequestBody PasswordDTO passwordDTO) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			userService.updatePassword(passwordDTO.getNewPassword(), passwordDTO.getConfirmPassword(),
					userDetailsDTO.getId());
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage("Update user password");
			apiResponse.setMsg(Message.builder().description("Update user password").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@ResponseBody
	@PostMapping("/update-mobile")
	public ResponseEntity updateMobileNumber(@RequestParam("mobile") String mobile) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			String otp=userService.updateMobileNumber(userDetailsDTO.getId(), mobile);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage("Update user mobile");
			apiResponse.setData(otp);
			apiResponse.setMsg(Message.builder().description("Update user mobile").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@ResponseBody
	@PostMapping("/validate-mobile")
	public ResponseEntity validateMobileNumber(@RequestParam("otp") String otp) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			userService.validateUpdatedMobileNumber(userDetailsDTO.getId(), otp);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage("Mobile update validated");
			apiResponse.setMsg(Message.builder().description("Mobile update validated").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/upload-image")
	@ResponseBody
	public ResponseEntity uploadImage(@RequestParam("file") MultipartFile file) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			String saveImage = userService.saveImage(file, userDetailsDTO.getId());
			apiResponse.setData(saveImage);
			apiResponse.setMessage("Image uploaded");
			apiResponse.setMsg(Message.builder().description("Image uploaded").title("Success").build());
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/get-image")
	@ResponseBody
	public ResponseEntity getImage(@RequestParam("id") String id) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			String saveImage = userService.getImage(id);
			apiResponse.setData(saveImage);
			apiResponse.setMessage("Image uploaded");
			apiResponse.setMsg(Message.builder().description("Image uploaded").title("Success").build());
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}


	@PostMapping("/register-phone")
	@ResponseBody
	public ResponseEntity saveSocialPhone(@RequestBody @Valid AddSocialPhoneDTO addSocialPhoneDTO) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(userService.savePhoneNumber(userDetailsDTO.getId(), addSocialPhoneDTO.getPhoneNumber()));
			apiResponse
					.setMessage("Register User Phone number successful:" + userVerificationRepository.findByUserId(userDetailsDTO.getId()).getOtp());
			apiResponse.setMsg(Message.builder().description("Phone number register successful").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

}
