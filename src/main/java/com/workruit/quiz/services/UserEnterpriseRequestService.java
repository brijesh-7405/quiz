/**
 * 
 */
package com.workruit.quiz.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.UserEnterpriseRequestDTO;
import com.workruit.quiz.persistence.entity.Enterprise;
import com.workruit.quiz.persistence.entity.User;
import com.workruit.quiz.persistence.entity.UserEnterpriseRequest;
import com.workruit.quiz.persistence.entity.repository.EnterpriseRepository;
import com.workruit.quiz.persistence.entity.repository.UserEnterpriseRequestRepository;
import com.workruit.quiz.persistence.entity.repository.UserRepository;

/**
 * @author Santosh Bhima
 *
 */
@Service
public class UserEnterpriseRequestService {

	private @Autowired UserEnterpriseRequestRepository userEnterpriseRequestRepository;
	private @Autowired UserRepository userRepository;
	private @Autowired EnterpriseRepository enterpriseRepository;

	public List<UserEnterpriseRequestDTO> listUserRequests(Long enterpriseId, int page, int size) {
		PageRequest newPAgeRequest = PageRequest.of(page, size,Sort.by(Sort.Direction.DESC,"modifiedDate"));
		List<UserEnterpriseRequest> userEnterpriseRequests = userEnterpriseRequestRepository
				.findByEnterpriseId(enterpriseId, newPAgeRequest);
		return userEnterpriseRequests.stream().map(request -> {
			UserEnterpriseRequestDTO userEnterpriseRequestDTO = new UserEnterpriseRequestDTO();
			userEnterpriseRequestDTO.setEnterpriseId(enterpriseId);
			User user = userRepository.findById(request.getUserId()).get();
			userEnterpriseRequestDTO.setName(user.getFirstName() + " " + user.getLastName());
			userEnterpriseRequestDTO.setUserId(request.getUserId());
			userEnterpriseRequestDTO.setStatus(request.isApproveStatus());
			userEnterpriseRequestDTO.setUserRejected(request.isUserRejected());
			userEnterpriseRequestDTO.setEmail(user.getPrimaryEmail());
			userEnterpriseRequestDTO.setPhoneNumber(user.getMobile());
			return userEnterpriseRequestDTO;
		}).collect(Collectors.toList()).stream().collect(Collectors.toList());
	}

	public Long count(Long enterpriseId) throws WorkruitException {
		if (enterpriseId == null) {
			throw new WorkruitException("Enterprise not found");
		}
		return userEnterpriseRequestRepository.countByEnterpriseId(enterpriseId);
	}

	public void updateUserStatus(Long enterpriseId, Long userId, boolean accepted) throws WorkruitException {
		Enterprise enterprise = enterpriseRepository.findById(enterpriseId).get();
		User user = userRepository.findById(userId).get();
		if (accepted) {
			if (user.getAccessCode() == null) {
				user.setAccessCode(enterprise.getEnterpriseCode());
			} else {
				String[] existingCodes = user.getAccessCode().split(",");
				boolean alreadyPresent = false;
				for (String existingCode : existingCodes) {
					if (existingCode.equalsIgnoreCase(enterprise.getEnterpriseCode())) {
						alreadyPresent = true;
						break;
					}
				}
				if (!alreadyPresent) {
					user.setAccessCode(user.getAccessCode() + "," + enterprise.getEnterpriseCode());
				}
			}
		}
		UserEnterpriseRequest userEnterpriseRequest = userEnterpriseRequestRepository
				.findByEnterpriseIdAndUserId(enterpriseId, userId);
		if (accepted) {
			userEnterpriseRequest.setApproveStatus(true);
			userEnterpriseRequest.setUserRejected(false);
		} else {
			userEnterpriseRequest.setApproveStatus(false);
			userEnterpriseRequest.setUserRejected(true);
			if(user.getAccessCode() != null) {
				List<String> currentAccessCode= new ArrayList<>(Arrays.asList(user.getAccessCode().split(",")));
				currentAccessCode.remove(enterprise.getEnterpriseCode());
				user.setAccessCode(currentAccessCode.stream().collect(Collectors.joining(",")));
			}
		}
		userRepository.save(user);
		userEnterpriseRequestRepository.save(userEnterpriseRequest);
	}
}
