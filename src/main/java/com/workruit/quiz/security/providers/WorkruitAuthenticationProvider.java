/**
 * 
 */
package com.workruit.quiz.security.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.EnterpriseDetails;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;
import com.workruit.quiz.persistence.entity.User;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.persistence.entity.repository.UserDetailsRepository;
import com.workruit.quiz.services.EnterpriseService;
import com.workruit.quiz.services.SubscriptionPlanFeatureMappingService;
import com.workruit.quiz.services.UserService;

/**
 * @author Santosh
 *
 */
public class WorkruitAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(WorkruitAuthenticationProvider.class);
	private @Autowired UserDetailsService userDetailsService;
	private @Autowired UserService userService;
	private EnterpriseService enterpriseService;
	private UserDetailsRepository userDetailsRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		logger.info("Authenticating the user");

		User user = getUserService().login(authentication.getPrincipal().toString(),
				authentication.getCredentials().toString(), authentication.getDetails());
		if (user == null) {
			user = getUserService().loginByMobile(authentication.getPrincipal().toString(),
					authentication.getCredentials().toString());
		}
		if (user != null) {
			UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
			userDetailsDTO.setUserRole(user.getUserRole().name());
			userDetailsDTO.setEmail(user.getPrimaryEmail());
			userDetailsDTO.setName(user.getFirstName() + " " + user.getLastName());
			userDetailsDTO.setPhone(user.getMobile());
			userDetailsDTO.setEnabled(user.isEnabled());
			if (user.getUserRole() == UserRole.ENTERPRISE_USER) {
				try {
					EnterpriseDetails enterpriseDetails = getEnterpriseService().getByEmail(user.getPrimaryEmail());
					if (enterpriseDetails != null) {
						userDetailsDTO.setEnterpriseId(enterpriseDetails.getEnterprise().getId());
						userDetailsDTO.setEnterpriseSubscriptionDetails(
								enterpriseService.getEnterpriseSubscription(enterpriseDetails.getEnterprise().getId()));
					}
				} catch (WorkruitException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (getUserDetailsRepository().findById(user.getId()).isPresent()) {
				userDetailsDTO.setDetailsPopulated(true);
			} else {
				userDetailsDTO.setDetailsPopulated(false);
			}
			userDetailsDTO.setId(user.getId());
			if (!user.isEnabled()) {
				try {
					throw new AuthenticationCredentialsNotFoundException(
							new ObjectMapper().writeValueAsString(userDetailsDTO));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
			return new UsernamePasswordAuthenticationToken(userDetailsDTO, null);
		} else {
			throw new AuthenticationCredentialsNotFoundException("Invalid Credentials");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.authentication.AuthenticationProvider#supports(
	 * java.lang.Class)
	 */
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public EnterpriseService getEnterpriseService() {
		return enterpriseService;
	}

	public void setEnterpriseService(EnterpriseService enterpriseService) {
		this.enterpriseService = enterpriseService;
	}

	public UserDetailsRepository getUserDetailsRepository() {
		return userDetailsRepository;
	}

	public void setUserDetailsRepository(UserDetailsRepository userDetailsRepository) {
		this.userDetailsRepository = userDetailsRepository;
	}

}
