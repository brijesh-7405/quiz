/**
 * 
 */
package com.workruit.quiz.controllers;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.ApiResponse;
import com.workruit.quiz.controllers.dto.EnterpriseDTO;
import com.workruit.quiz.controllers.dto.EnterpriseDetails;
import com.workruit.quiz.controllers.dto.Message;
import com.workruit.quiz.controllers.dto.PageApiResponse;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.security.UserAuthorized;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.EnterpriseService;

/**
 * @author Santosh
 *
 */
@Controller
public class EnterpriseController {

	private @Autowired EnterpriseService enterpriseService;
	private @Autowired MessageSource messageSource;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PostMapping("/enterprise")
	@UserAuthorized(userRoles = UserRole.ENTERPRISE_USER)
	public ResponseEntity create(@RequestBody @Valid EnterpriseDTO enterpriseDTO) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			if (!userDetailsDTO.getEmail().equals(enterpriseDTO.getContactEmail())) {
				return new ResponseEntity<>("Please use the enterprise user email", HttpStatus.UNAUTHORIZED);
			}
			long id = enterpriseService.save(enterpriseDTO, userDetailsDTO.getId() ,false);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(id);
			apiResponse.setMessage("Enterprise created successfully");
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("enterprise.create.success.description", null, null))
					.title(messageSource.getMessage("enterprise.create.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("enterprise.create.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@PutMapping("/enterprise/{id}")
	@UserAuthorized(userRoles = UserRole.ENTERPRISE_USER)
	public ResponseEntity update(@RequestBody @Valid EnterpriseDTO enterpriseDTO,
			@PathVariable("id") Long enterpriseId) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			EnterpriseDetails details = enterpriseService.getByEmail(userDetailsDTO.getEmail());
			if (details.getEnterprise().getId().longValue() != enterpriseId.longValue()
					|| !userDetailsDTO.getEmail().equals(enterpriseDTO.getContactEmail())) {
				return new ResponseEntity<>("Cannot update other enterprise details", HttpStatus.UNAUTHORIZED);
			}
			enterpriseDTO.setId(enterpriseId);
			long id = enterpriseService.save(enterpriseDTO, userDetailsDTO.getId(), true);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(id);
			apiResponse.setMessage("Enterprise updated successfully");
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("enterprise.update.success.description",
							new Object[] { enterpriseDTO.getId() }, null))
					.title(messageSource.getMessage("enterprise.update.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("enterprise.update.success.status", null, null));
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@GetMapping("/enterprise")
	@UserAuthorized(userRoles = UserRole.ENTERPRISE_USER)
	public ResponseEntity get() {
		try {
			ApiResponse apiResponse = new ApiResponse();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			EnterpriseDetails enterprise = enterpriseService.getByEmail(userDetailsDTO.getEmail());
			apiResponse.setData(enterprise);
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("enterprise.get.success.description",
							new Object[] { enterprise.getEnterprise().getId() }, null))
					.title(messageSource.getMessage("enterprise.get.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("enterprise.get.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@GetMapping("/enterprise/{id}")
	@UserAuthorized(userRoles = UserRole.SUPERADMIN)
	public ResponseEntity get(@PathVariable("id") Long id) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(enterpriseService.get(id));
			apiResponse.setMsg(Message.builder()
					.description(
							messageSource.getMessage("enterprise.get.success.description", new Object[] { id }, null))
					.title(messageSource.getMessage("enterprise.get.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("enterprise.get.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@GetMapping("/enterprise/list")
	public ResponseEntity listenterprises(@RequestParam("page") int page, @RequestParam("size") int size,
			@RequestParam(value = "sortBy", defaultValue = "enterpriseName:ASC") String sortBy) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();

			PageApiResponse apiResponse = new PageApiResponse();
			apiResponse.setMessage("Enterprise Listing Details");
			apiResponse.setData(enterpriseService.list(page, size, sortBy, userDetailsDTO.getId()));
			apiResponse.setNumberOfRecords(enterpriseService.listCount(userDetailsDTO.getId()));
			apiResponse.setPage(page);
			apiResponse.setSize(size);
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("enterprise.list.success.description",
							new Object[] { page, size }, null))
					.title(messageSource.getMessage("enterprise.list.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("enterprise.list.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@GetMapping("/enterprise/list-admin")
	@UserAuthorized(userRoles = UserRole.SUPERADMIN)
	public ResponseEntity list(@RequestParam("page") int page, @RequestParam("size") int size) {
		try {
			PageApiResponse apiResponse = new PageApiResponse();
			apiResponse.setMessage("Enterprise Listing Details");
			apiResponse.setData(enterpriseService.listForAdmin(page, size));
			apiResponse.setNumberOfRecords(enterpriseService.count());
			apiResponse.setPage(page);
			apiResponse.setSize(size);
			apiResponse.setMsg(Message.builder()
					.description(messageSource.getMessage("enterprise.list.success.description",
							new Object[] { page, size }, null))
					.title(messageSource.getMessage("enterprise.list.success.title", null, null)).build());
			apiResponse.setStatus(messageSource.getMessage("enterprise.list.success.status", null, null));
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@PostMapping("/enterprise/{enterpriseId}/authorize")
	@UserAuthorized(userRoles = UserRole.SUPERADMIN)
	public ResponseEntity verifyEnterprise(@PathVariable("enterpriseId") Long enterpriseId) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage("Enterprise is Verified");
			enterpriseService.authorize(enterpriseId);
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (WorkruitException we) {
			return ControllerUtils.customErrorMessage(we.getMessage());
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/enterprise/upload-image")
	@ResponseBody
	@UserAuthorized(userRoles = UserRole.ENTERPRISE_USER)
	public ResponseEntity uploadImage(@RequestParam("file") MultipartFile file) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			String saveImage = enterpriseService.saveImage(file, userDetailsDTO.getEnterpriseId());
			apiResponse.setData(saveImage);
			apiResponse.setMessage("Image uploaded");
			apiResponse.setMsg(Message.builder().description("Image uploaded").title("Success").build());
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/enterprise/get-image")
	@ResponseBody
	public ResponseEntity getImage(@RequestParam("id") String id) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			String saveImage = enterpriseService.getImage(id);
			apiResponse.setData(saveImage);
			apiResponse.setMessage("Image uploaded");
			apiResponse.setMsg(Message.builder().description("Get Image").title("Success").build());
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}
}
