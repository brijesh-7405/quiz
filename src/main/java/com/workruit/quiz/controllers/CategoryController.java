/**
 * 
 */
package com.workruit.quiz.controllers;

import java.util.List;

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

import com.workruit.quiz.controllers.dto.ApiResponse;
import com.workruit.quiz.controllers.dto.CategoryDTO;
import com.workruit.quiz.controllers.dto.Message;
import com.workruit.quiz.controllers.dto.PageApiResponse;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.security.UserAuthorized;
import com.workruit.quiz.security.utils.ControllerUtils;
import com.workruit.quiz.services.CategoryService;

/**
 * @author Santosh
 *
 */
@Controller
public class CategoryController {
	private @Autowired CategoryService categoryService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/category")
	@ResponseBody
	public ResponseEntity create(@RequestBody @Valid CategoryDTO categoryDTO) {
		try {
			Long id = categoryService.save(categoryDTO);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(id);
			apiResponse.setMessage("Category/Topic created successfully");
			apiResponse.setMsg(Message.builder().description("Created Category").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.CREATED);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@GetMapping("/category/list")
	@ResponseBody
	public ResponseEntity<List<CategoryDTO>> list(@RequestParam("page") int pageNumber,
			@RequestParam("size") int size) {
		try {
			List<CategoryDTO> list = categoryService.list(pageNumber, size);
			PageApiResponse apiResponse = new PageApiResponse();
			apiResponse.setData(list);
			apiResponse.setMessage("Fetching categories list");
			apiResponse.setPage(pageNumber);
			apiResponse.setSize(size);
			apiResponse.setNumberOfRecords(categoryService.count());
			apiResponse.setMsg(Message.builder().description("List Categories").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/category/listByIds/{ids}")
	@ResponseBody
	public ResponseEntity<List<CategoryDTO>> filter(@PathVariable("ids") List<Long> ids) {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(categoryService.getByMultiple(ids));
			apiResponse.setMessage("Fetched multiple categories");
			apiResponse.setMsg(Message.builder().description("Created Category").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/category/{id}")
	@ResponseBody
	public ResponseEntity get(@PathVariable("id") Long id) {
		try {
			CategoryDTO dto = categoryService.get(id);
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(dto);
			apiResponse.setMsg(Message.builder().description("Get Category").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@GetMapping("/category/details")
	@UserAuthorized(userRoles = { UserRole.ENTERPRISE_USER, UserRole.SUPERADMIN })
	public ResponseEntity categoryDetails() {
		try {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(categoryService.categoryDetails());
			apiResponse.setMessage("Fetched multiple categories");
			apiResponse.setMsg(Message.builder().description("Fetch Categories").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping(path = { "/category/filter/enterprise/{enterpriseId}/categories/{categoryId}",
			"/category/filter/enterprise/{enterpriseId}/categories/",
			"/category/filter/enterprise//categories/{categoryId}", "/category/filter/enterprise//categories/" })
	public ResponseEntity filterByCategory(@PathVariable(value = "enterpriseId", required = false) Long enterpriseId,
			@PathVariable(value = "categoryId", required = false) Long categoryId) {
		try {
			UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setData(categoryService.filter(enterpriseId, categoryId, userDetailsDTO.getId()));
			apiResponse.setMessage("Filtered categories");
			apiResponse.setMsg(Message.builder().description("Filter Categories").title("Success").build());
			apiResponse.setStatus("Success");
			return new ResponseEntity(apiResponse, HttpStatus.OK);
		} catch (Exception e) {
			return ControllerUtils.genericErrorMessage();
		}
	}
}
