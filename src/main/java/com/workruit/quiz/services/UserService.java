/**
 * 
 */
package com.workruit.quiz.services;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.workruit.quiz.configuration.ConflictException;
import com.workruit.quiz.configuration.FieldValidationException;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.ExceptionResponse.FieldError;
import com.workruit.quiz.controllers.dto.*;
import com.workruit.quiz.persistence.entity.*;
import com.workruit.quiz.persistence.entity.Enterprise.EnterpriseType;
import com.workruit.quiz.persistence.entity.User.UserRole;
import com.workruit.quiz.persistence.entity.repository.*;
import com.workruit.quiz.security.utils.JwtTokenSettings;
import com.workruit.quiz.services.utils.DateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Santosh
 *
 */
@Service
@Slf4j
public class UserService {
	private @Autowired ModelMapper modelMapper;
	private @Autowired UserRepository userRepository;
	private @Autowired PasswordEncoder passwordEncoder;
	private @Autowired UserDetailsRepository userDetailsRepository;
	private @Autowired UserCategoriesRepository userCategoriesRepository;
	private @Autowired UserTopicsRepository userTopicsRepository;
	private @Autowired UserLocationRepository userLocationRepository;
	private @Autowired UserQuizService userQuizService;
	private @Autowired UserVerificationRepository userVerificationRepository;
	private @Autowired EnterpriseRepository enterpriseRepository;
	private @Autowired UserQuizAnalyticsDataRepository userQuizAnalyticsDataRepository;
	private @Autowired EnterpriseService enterpriseService;
	@Value("${SMS_URL}")
	private String smsURL;
	@Value("${SMS_USER}")
	private String smsUsername;
	@Value("${SMS_PASS}")
	private String smsPassword;
	@Value("${SMS_FROM}")
	private String smsFrom;
	private @Autowired JwtTokenSettings jwtSettings;
	private Map<Long, String> userOtps = new HashMap<>();
	@Value("${user.images.bucket}")
	private String userImagesBucket;
	private @Autowired AWSService awsService;
	private @Autowired UserEnterpriseRequestRepository userEnterpriseRequestRepository;

	@PostConstruct
	public void init() {
		PropertyMap<User, UserAnalyticsDTO> propertyMap = new PropertyMap<User, UserAnalyticsDTO>() {
			@Override
			protected void configure() {
				map().setEmail(source.getPrimaryEmail());
			}
		};
		modelMapper.addMappings(propertyMap);
	}

	public void addCode(String code, Long userId) throws WorkruitException {
		Enterprise enterprise = enterpriseRepository.findByEnterpriseCodeAndEnterpriseType(code,
				EnterpriseType.PRIVATE);
		if (enterprise == null) {
			throw new WorkruitException("Enterprise does not exists with this code or enterprise is not Private");
		}
		User user = userRepository.findById(userId).get();
		if (user.getAccessCode() == null) {
			//user.setAccessCode(code);
			UserEnterpriseRequest existing = userEnterpriseRequestRepository
					.findByEnterpriseIdAndUserId(enterprise.getId(), userId);
			if (existing != null) {
				throw new WorkruitException("Enterprise Code is already added for the user.");
			}
			UserEnterpriseRequest userEnterpriseRequest = new UserEnterpriseRequest();
			userEnterpriseRequest.setUserId(userId);
			userEnterpriseRequest.setEnterpriseId(enterprise.getId());
			userEnterpriseRequest.setApproveStatus(false);
			userEnterpriseRequestRepository.save(userEnterpriseRequest);
		} else {
			List<String> existingCodes = new ArrayList<>(Arrays.asList(user.getAccessCode().split(",")));
			for (String existingCode : existingCodes) {
				if (existingCode.equalsIgnoreCase(code)) {
					throw new WorkruitException("Enterprise Code is already added for the user.");
				}
			}
			existingCodes.add(code);
			//user.setAccessCode(existingCodes.stream().collect(Collectors.joining(",")));
			UserEnterpriseRequest userEnterpriseRequest = new UserEnterpriseRequest();
			userEnterpriseRequest.setUserId(userId);
			userEnterpriseRequest.setEnterpriseId(enterprise.getId());
			userEnterpriseRequest.setApproveStatus(false);
			userEnterpriseRequestRepository.save(userEnterpriseRequest);
		}
		userRepository.save(user);
	}

	public List<EnterpriseCodeDetailsDTO> getCodeDetails(Long userId) {
		Optional<User> userOptional = userRepository.findById(userId);
		List<EnterpriseCodeDetailsDTO> enterpriseCodeDetailsDTOs = new ArrayList<>();
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if (StringUtils.isNotBlank(userOptional.get().getAccessCode())) {
				String[] codes = user.getAccessCode().split(",");
				for (String code : codes) {
					Enterprise enterprise = enterpriseRepository.findByEnterpriseCodeAndEnterpriseType(code,
							EnterpriseType.PRIVATE);
					if (enterprise != null) {
						EnterpriseCodeDetailsDTO enterpriseCodeDetailsDTO = createEnterpriseCodeDetailsDTO(enterprise,code, EnterpriseCodeDetailsDTO.Status.APPROVED);
						enterpriseCodeDetailsDTOs.add(enterpriseCodeDetailsDTO);
					}
				}
			}
			List<UserEnterpriseRequest> userEnterpriseRequests = userEnterpriseRequestRepository.findByUserId(user.getId());
			for (UserEnterpriseRequest userEnterpriseRequest : userEnterpriseRequests) {
				if (!userEnterpriseRequest.isApproveStatus() && !userEnterpriseRequest.isUserRejected()) {
					Enterprise enterprise = enterpriseRepository.findByIdAndEnterpriseType(userEnterpriseRequest.getEnterpriseId(), EnterpriseType.PRIVATE);
					if (enterprise != null) {
						EnterpriseCodeDetailsDTO enterpriseCodeDetailsDTO = createEnterpriseCodeDetailsDTO(enterprise,enterprise.getEnterpriseCode(),EnterpriseCodeDetailsDTO.Status.PENDING);
						enterpriseCodeDetailsDTOs.add(enterpriseCodeDetailsDTO);
					}
				} else if (!userEnterpriseRequest.isApproveStatus() && userEnterpriseRequest.isUserRejected()) {
					Enterprise enterprise = enterpriseRepository.findByIdAndEnterpriseType(userEnterpriseRequest.getEnterpriseId(), EnterpriseType.PRIVATE);
					if (enterprise != null) {
						EnterpriseCodeDetailsDTO enterpriseCodeDetailsDTO = createEnterpriseCodeDetailsDTO(enterprise,enterprise.getEnterpriseCode(),EnterpriseCodeDetailsDTO.Status.REJECTED);
						enterpriseCodeDetailsDTOs.add(enterpriseCodeDetailsDTO);
					}
				}
			}
		}
		return enterpriseCodeDetailsDTOs;
	}

	private EnterpriseCodeDetailsDTO createEnterpriseCodeDetailsDTO(Enterprise enterprise, String code, EnterpriseCodeDetailsDTO.Status status) {
		EnterpriseCodeDetailsDTO enterpriseCodeDetailsDTO = new EnterpriseCodeDetailsDTO();
		enterpriseCodeDetailsDTO.setCode(code);
		enterpriseCodeDetailsDTO.setEnterpriseName(enterprise.getName());
		enterpriseCodeDetailsDTO.setEnterpriseId(enterprise.getId());
		enterpriseCodeDetailsDTO.setStatus(status);
		return enterpriseCodeDetailsDTO;
	}

	public User login(String username, String password, Object token) {
		User user = userRepository.findByPrimaryEmail(username);
		if (user == null) {
			return null;
		}
		if (user.getLoginType().equalsIgnoreCase("FACEBOOK") || user.getLoginType().equalsIgnoreCase("GOOGLE")) {
			if (token != null) {
				return user;
			} else {
				return null;
			}
		}
		if (passwordEncoder.matches(password, user.getPassword())) {
			return user;
		} else {
			return null;
		}
	}

	@Transactional
	public void profileUpdate(UserInterestsDTO userInterestsDTO, Long userId) throws ParseException {
		try {
			Optional<User> user = userRepository.findById(userId);
			DateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy HH:mm");
			Date date = dateFormat.parse(userInterestsDTO.getDob());
			user.get().setDob(date);
			userRepository.save(user.get());
			UserDetails userDetails = new UserDetails();
			userDetails.setUserId(userId);
			userDetails.setCollegeName(userInterestsDTO.getCollegeName());
			userDetails.setCurrentCompanyName(userInterestsDTO.getCurrentCompanyName());
			userDetails.setSummary(userInterestsDTO.getAboutMe());
			userDetails.setDescription(userInterestsDTO.getDescription());
			userDetailsRepository.save(userDetails);
			userCategoriesRepository.deleteByUserId(userId);
			userTopicsRepository.deleteByUserId(userId);
			if (userInterestsDTO.getInterestedCategories() != null) {
				userCategoriesRepository.saveAll(userInterestsDTO.getInterestedCategories().stream()
						.map(categoryId -> new UserCategories(userId, categoryId)).collect(Collectors.toList()));
			}
			if (userInterestsDTO.getInterestedTopics() != null) {
				userTopicsRepository.saveAll(userInterestsDTO.getInterestedTopics().stream()
						.map(topicId -> new UserTopics(userId, topicId)).collect(Collectors.toList()));
			}
			UserLocation userLocation = userLocationRepository.findByUserId(userId);
			if (userLocation == null) {
				userLocationRepository.save(new UserLocation(userId, userInterestsDTO.getCity(),
						userInterestsDTO.getState(), userInterestsDTO.getCountry(), userInterestsDTO.getAddress()));
			} else {
				userLocation.setAddress(userInterestsDTO.getAddress());
				userLocation.setCity(userInterestsDTO.getCity());
				userLocation.setCountry(userInterestsDTO.getCountry());
				userLocation.setState(userInterestsDTO.getState());
				userLocationRepository.save(userLocation);
			}
		} catch (ParseException pe) {
			log.error("Unable to parse date", pe);
			throw pe;
		} catch (Exception e) {
			log.error("Error while saving user details,user categories, user topic", e);
			throw e;
		}
	}

	@Transactional(readOnly = true)
	public UserInterestsDTO profileDetails(Long userId) {
		try {
			UserInterestsDTO userInterestsDTO = new UserInterestsDTO();
			UserDetails userDetails = userDetailsRepository.findById(userId).get();
			userInterestsDTO.setAboutMe(userDetails.getSummary());
			userInterestsDTO.setDescription(userDetails.getDescription());

			userInterestsDTO.setCurrentCompanyName(userDetails.getCurrentCompanyName());
			userInterestsDTO.setCollegeName(userDetails.getCollegeName());
			UserLocation userLocation = userLocationRepository.findByUserId(userId);
			userInterestsDTO.setCity(userLocation.getCity());
			userInterestsDTO.setState(userLocation.getState());
			userInterestsDTO.setCountry(userLocation.getCountry());
			userInterestsDTO.setAddress(userLocation.getAddress());
			userInterestsDTO.setInterestedCategories(userCategoriesRepository.findByUserId(userId).stream()
					.map(userCategory -> userCategory.getCategoryId()).collect(Collectors.toList()));
			userInterestsDTO.setInterestedTopics(userTopicsRepository.findByUserId(userId).stream()
					.map(userTopic -> userTopic.getTopicId()).collect(Collectors.toList()));
			User user = userRepository.findById(userId).get();
			userInterestsDTO.setDob(DateUtils.format(user.getDob()));
			return userInterestsDTO;
		} catch (Exception e) {
			log.error("Error while retrieving data for user details", e);
			throw e;
		}
	}

	@Transactional
	public void update(UserProfileDTO userProfileDTO) throws Exception {
		try {
			Optional<User> user = userRepository.findById(userProfileDTO.getId());
			if (user != null && user.isPresent()) {
				User u = user.get();
				User existingUser = userRepository.findByPrimaryEmail(userProfileDTO.getPrimaryEmail());
				if (existingUser != null && existingUser.getId() != userProfileDTO.getId()) {
					throw new ConflictException(
							"There is existing user with email:" + userProfileDTO.getPrimaryEmail());
				}
				u.setProfileImageUrl(userProfileDTO.getProfileImageUrl());
				u.setFirstName(userProfileDTO.getFirstName());
				u.setLastName(userProfileDTO.getLastName());
				u.setPrimaryEmail(userProfileDTO.getPrimaryEmail());
				userRepository.save(u);
				updateUserQuizDetails(u);
			} else {
				throw new Exception("User not found");
			}
		} catch (ConflictException e) {
			throw new WorkruitException(e.getConflict());
		}
	}

	private void updateUserQuizDetails(User u) {
		List<UserQuizAnalyticsData> userQuizAnalyticsDatas = userQuizAnalyticsDataRepository.findByUserId(u.getId());
		for (UserQuizAnalyticsData userQuizAnalyticsData : userQuizAnalyticsDatas) {
			userQuizAnalyticsData.setEmail(u.getPrimaryEmail());
			userQuizAnalyticsData.setFirstName(u.getFirstName());
			userQuizAnalyticsData.setLastName(u.getLastName());
			userQuizAnalyticsDataRepository.save(userQuizAnalyticsData);
		}
	}

	public String sendOTP(String mobile) throws Exception {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			String otp = String.format("%06d", new Random().nextInt(999999));
			if (otp != null) {
				return otp;
			}
			String text = "Workruit Quiz User authorization code " + otp;
			HttpGet httpGet = new HttpGet(smsURL + "?username=" + smsUsername + "&password=" + smsPassword + "&from="
					+ smsFrom + "&udh=0&text=" + URLEncoder.encode(text) + "&to=" + mobile);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String output = IOUtils.toString(httpResponse.getEntity().getContent());
			if (!StringUtils.equalsIgnoreCase(output, "sent.")) {
				// throw new Exception("Unable to send code");
			}
			return otp;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mobile;
	}

	public User loginByMobile(String mobile, String otp) {
		User user = userRepository.findByMobile(mobile);
		if (user != null) {
			UserVerification userVerification = userVerificationRepository.findByUserIdAndOtp(user.getId(), otp);
			if (userVerification != null) {
				return user;
			} else {
				return null;
			}
		}
		return null;
	}

	public String getOTP(Long userId) {
		return userVerificationRepository.findByUserId(userId).getOtp();
	}

	public String sendOTP(Long id) throws Exception {
		Optional<User> findById = userRepository.findById(id);
		if (findById.isPresent()) {
			User user = findById.get();
			if (!user.isEnabled()) {
				String otp = sendOTP(user.getMobile());
				updateUserVerification(user, otp);
				return otp;
			} else {
				throw new WorkruitException("User already verified");
			}
		} else {
			throw new WorkruitException("User has not signed up yet.");
		}
	}

	public String updateMobileNumber(Long userId, String mobile) throws Exception {
		User existingUser = userRepository.findByMobile(mobile);
		if (existingUser != null) {
			throw new WorkruitException("User exists the same mobile: " + mobile);
		}
		String otp = sendOTP(mobile);
		userOtps.put(userId, mobile + ":" + otp);
		return otp;
	}

	public void validateUpdatedMobileNumber(Long userId, String otp) throws WorkruitException {
		String userOtp = userOtps.get(userId).split(":")[1];
		if (!StringUtils.equals(userOtp, otp)) {
			throw new WorkruitException("Wrong OTP");
		}
		User user = userRepository.findById(userId).get();
		user.setMobile(userOtps.get(userId).split(":")[0]);
		userRepository.save(user);
	}

	public Map<String, Object> sendOTPforLogin(String mobile) throws Exception {
		User user = userRepository.findByMobile(mobile);
		if (user == null) {
			throw new WorkruitException("No user exists");
		}
		Map<String, Object> response = new HashMap<>();
		response.put("userId", user.getId());
		if (!user.isEnabled()) {
			response.put("signupProcessComplete", false);
			response.put("userType", user.getUserRole().name());
		} else {
			String otp = sendOTP(user.getMobile());
			updateUserVerification(user, otp);
			response.put("signupProcessComplete", true);
			response.put("userType", user.getUserRole().name());
			response.put("otp", otp);
		}
		return response;
	}

	public Map<String, Object> validateUser(String mobile) throws Exception {
		User user = userRepository.findByMobile(mobile);
		if (user == null) {
			throw new WorkruitException("No user exists");
		}
		Map<String, Object> response = new HashMap<>();
		response.put("userId", user.getId());
		if (!user.isEnabled()) {
			response.put("signupProcessComplete", false);
			response.put("userType", user.getUserRole().name());
		} else {
			response.put("signupProcessComplete", true);
			response.put("userType", user.getUserRole().name());
		}
		return response;
	}

	private void updateUserVerification(User user, String otp) {
		UserVerification userVerification = userVerificationRepository.findByUserId(user.getId());
		if (userVerification != null) {
			userVerification.setOtp(otp);
		} else {
			userVerification = new UserVerification();
			userVerification.setUserId(user.getId());
			userVerification.setOtp(otp);
		}
		userVerificationRepository.save(userVerification);
	}

	@Transactional
	public Map<String, String> saveSocial(UserSocialSignupProfileDTO userProfileDTO) throws Exception {
		try {
			if (userProfileDTO.getUserRole() == UserRole.SUPERADMIN) {
				if (userRepository.findByUserRole(UserRole.SUPERADMIN).size() >= 1) {
					throw new WorkruitException("Only One SuperAdmin can exist");
				}
			}
			log.debug("Saving user profile:{}", userProfileDTO);
			User existingUser = userRepository.findByPrimaryEmail(userProfileDTO.getPrimaryEmail());
			if (existingUser != null) {
				return generateToken(existingUser);
			}
			Converter<String, Date> dateConverter = new AbstractConverter<String, Date>() {
				@Override
				protected Date convert(String source) {
					try {
						return new SimpleDateFormat("dd MMM, yyyy HH:mm").parse(source);
					} catch (ParseException e) {
						log.error("Error while parsing the date", e);
					}
					return null;
				}
			};
			modelMapper.addConverter(dateConverter);
			User user = modelMapper.map(userProfileDTO, User.class);
			List<FieldError> fieldErrors = new ArrayList<>();
			if (user.getLoginType() != null && (user.getLoginType().equalsIgnoreCase("FACEBOOK")
					|| user.getLoginType().equalsIgnoreCase("GOOGLE"))) {
				user.setPassword(RandomStringUtils.randomAlphanumeric(10));
			}
			user.setProfileImageUrl(userProfileDTO.getProfileImageUrl());
			user.setUserRole(userProfileDTO.getUserRole());
			user.setMobile("0000000000");
			user.setEnabled(false);

			if (fieldErrors.size() == 0) {
				user = userRepository.save(user);
			} else {
				throw new FieldValidationException(fieldErrors);
			}
			return generateToken(user);
		} catch (Exception e) {
			log.error("Error while saving User Profile", e);
			throw e;
		}
	}

	@Transactional
	public void updatePassword(String newPassword, String confirmPassword, Long userId) throws Exception {
		try {
			Optional<User> optionalUser = userRepository.findById(userId);
			User user = null;
			if (!optionalUser.isPresent()) {
				throw new WorkruitException("User not present in the system");
			} else {
				user = optionalUser.get();
			}
			if (!user.isEnabled()) {
				throw new WorkruitException("User is not yet enabled to perform this operation");
			}
			if (user.getLoginType() != null && !user.getLoginType().equals("FORM")) {
				throw new WorkruitException("User is not yet enabled to perform this operation");
			}
			Optional<UserDetails> optionalUserDetails = userDetailsRepository.findById(userId);

			if (!optionalUserDetails.isPresent()) {
				if (user.getUserRole() == UserRole.DEFAULT_USER) {
					throw new WorkruitException("User has not completely filled the details");
				}
			}

			if (!StringUtils.equals(newPassword, confirmPassword)) {
				throw new WorkruitException("Confirm password and New password are not same");
			}
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);
		} catch (WorkruitException we) {
			throw we;
		} catch (Exception e) {
			throw e;
		}
	}

	@Transactional
	public long save(UserSignupProfileDTO userProfileDTO) throws Exception {
		try {
			if (userProfileDTO.getUserRole() == UserRole.SUPERADMIN) {
				if (userRepository.findByUserRole(UserRole.SUPERADMIN).size() >= 1) {
					throw new WorkruitException("Only One SuperAdmin can exist");
				}
			}
			log.debug("Saving user profile:{}", userProfileDTO);
			User existingUser = userRepository.findByMobile(userProfileDTO.getMobile());
			if (existingUser != null) {
				throw new ConflictException("There is existing user with mobile:" + userProfileDTO.getMobile());
			}
			existingUser = userRepository.findByPrimaryEmail(userProfileDTO.getPrimaryEmail());
			if (existingUser != null) {
				throw new ConflictException("There is existing user with email:" + userProfileDTO.getPrimaryEmail());
			}
			Converter<String, Date> dateConverter = new AbstractConverter<String, Date>() {
				@Override
				protected Date convert(String source) {
					try {
						return new SimpleDateFormat("dd MMM, yyyy HH:mm").parse(source);
					} catch (ParseException e) {
						log.error("Error while parsing the date", e);
					}
					return null;
				}
			};
			modelMapper.addConverter(dateConverter);
			User user = modelMapper.map(userProfileDTO, User.class);
			List<FieldError> fieldErrors = new ArrayList<>();
			if (user.getLoginType() != null && user.getLoginType().equalsIgnoreCase("FORM")) {
				if (!userProfileDTO.getPassword().equals(userProfileDTO.getConfirmPassword())) {
					fieldErrors.add(new FieldError("password", "Password and Confirm Password are not same"));
				}
				if (StringUtils.isNotBlank(user.getPassword())) {
					user.setPassword(passwordEncoder.encode(user.getPassword()));
				} else {
					// TODO: Throw Exception
				}
			} else {
				user.setPassword(passwordEncoder.encode("TEST"));
			}
			user.setEnabled(false);
			if (fieldErrors.size() == 0) {
				user = userRepository.save(user);
			} else {
				throw new FieldValidationException(fieldErrors);
			}
			user.setProfileImageUrl(userProfileDTO.getProfileImageUrl());
			user.setUserRole(userProfileDTO.getUserRole());
			String otp = sendOTP(user.getMobile());
			UserVerification userVerification = new UserVerification();
			userVerification.setUserId(user.getId());
			userVerification.setOtp(otp);
			if (userVerificationRepository.findByUserId(user.getId()) != null) {
				userVerification = userVerificationRepository.findByUserId(user.getId());
			}
			userVerificationRepository.save(userVerification);
			return user.getId();
		} catch (Exception e) {
			log.error("Error while saving User Profile", e);
			throw e;
		}
	}

	public Map<String, String> generateToken(User user) throws JsonProcessingException {
		Map<String, String> map = new HashMap<>();
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
		userDetailsDTO.setId(user.getId());
		userDetailsDTO.setUserRole(user.getUserRole().name());
		userDetailsDTO.setEmail(user.getPrimaryEmail());
		userDetailsDTO.setName(user.getFirstName() + " " + user.getLastName());
		userDetailsDTO.setPhone(user.getMobile());
		userDetailsDTO.setEnabled(user.isEnabled());
		if (user.getUserRole() == UserRole.ENTERPRISE_USER) {
			Enterprise enterpriseDetails = enterpriseRepository.findByContactEmail(user.getPrimaryEmail());
			if (enterpriseDetails != null) {
				userDetailsDTO.setEnterpriseId(enterpriseDetails.getId());
			}
		}
		if (userDetailsRepository.findById(user.getId()).isPresent()) {
			userDetailsDTO.setDetailsPopulated(true);
		} else {
			userDetailsDTO.setDetailsPopulated(false);
		}

		Map<String, Object> userDetails = new HashMap<>();
		userDetails.put("username", userDetailsDTO);
		Claims claims = Jwts.claims(userDetails);
		claims.setId(user.getId().toString());
		String token = getAccessToken(claims);
		map.put("access_token", token);
		map.put("iat", claims.getIssuedAt().toLocaleString());
		// map.put("eat", claims.getExpiration().toLocaleString());
		return map;
	}

	public String getAccessToken(Claims claims) {
		LocalDateTime localDateTime = LocalDateTime.now();
		String token = Jwts.builder().setClaims(claims).setIssuer(jwtSettings.getTokenIssuer())
				.setIssuedAt(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))
				.setExpiration(
						Date.from(localDateTime.plusMinutes(Integer.parseInt(jwtSettings.getTokenExpirationTime()))
								.atZone(ZoneId.systemDefault()).toInstant()))
				.signWith(SignatureAlgorithm.RS512, jwtSettings.getKey()).compact();
		return token;
	}

	@Transactional
	public Map<String, String> verifyUser(Long userId, String otp) throws WorkruitException, JsonProcessingException {
		UserVerification userVerification = userVerificationRepository.findByUserIdAndOtp(userId, otp);
		if (userVerification == null) {
			throw new WorkruitException("Wrong  OTP");
		}
		User user = userRepository.findById(userId).get();
		user.setEnabled(true);
		userRepository.save(user);
		return generateToken(user);
	}

	public UserProfileDTO get(long id) throws Exception {
		try {
			log.debug("Get user by id:{}", id);
			Optional<User> optionalUser = userRepository.findById(id);
			if (optionalUser.isPresent()) {
				optionalUser.get().setPassword(null);
				UserProfileDTO userProfileDTO = modelMapper.map(optionalUser.get(), UserProfileDTO.class);
				if (optionalUser.get().getLoginType().equals("FORM")) {
					if (StringUtils.isNotEmpty(optionalUser.get().getProfileImageUrl())) {
						userProfileDTO.setProfileImageUrl(getImage(optionalUser.get().getProfileImageUrl()));
						userProfileDTO.setProfileImageUrlKey(optionalUser.get().getProfileImageUrl());
					}
				} else {
					if (StringUtils.isNotBlank(optionalUser.get().getProfileImageUrl())) {
						if(isValidUrl(optionalUser.get().getProfileImageUrl())){
							URL url = new URL(optionalUser.get().getProfileImageUrl());
							userProfileDTO.setProfileImageUrl(Base64.getEncoder().encodeToString(IOUtils.toByteArray(url)));
						}else {
							userProfileDTO.setProfileImageUrl(getImage(optionalUser.get().getProfileImageUrl()));
						}
						userProfileDTO.setProfileImageUrlKey(optionalUser.get().getProfileImageUrl());
					}
				}
				return userProfileDTO;
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("Error while loading user profile", e);
			throw e;
		}
	}

	public Long count() {
		List<UserRole> roles = new ArrayList<>();
		roles.add(UserRole.DEFAULT_USER);
		return userRepository.countByUserRoleIn(roles);
	}

	public List<UserAnalyticsDTO> getUsers(int page, int size) {
		try {
			List<UserAnalyticsDTO> userAnalyticsDTOs = userRepository
					.findByUserRole(UserRole.DEFAULT_USER, PageRequest.of(page, size)).stream()
					.map(obj -> modelMapper.map(obj, UserAnalyticsDTO.class)).collect(Collectors.toList());

			userAnalyticsDTOs.stream().forEach(user -> {
				try {
					UserLocation userLocation = userLocationRepository.findByUserId(user.getId());
					if (userLocation != null) {
						user.setLocation(userLocation.getCity());
					}
					Optional<UserDetails> userDetails = userDetailsRepository.findById(user.getId());
					if (userDetails.isPresent()) {
						user.setCollege(userDetails.get().getCollegeName());
					}
					user.setQuizsTaken(userQuizService.countByUserId(user.getId()));
					user.setUserRegistrationDate(DateUtils.format(user.getCreatedDate()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			return userAnalyticsDTOs;
		} catch (Exception e) {
			throw e;
		}
	}

	public String saveImage(MultipartFile multipartFile, Long userId) throws IOException {
		String bucketName = userImagesBucket;
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(multipartFile.getContentType());
		String key = RandomUtils.nextLong(0, Long.MAX_VALUE) + "";
		User user = userRepository.findById(userId).get();
		if (StringUtils.isBlank(user.getProfileImageUrl()) || isValidUrl(user.getProfileImageUrl())) {
			PutObjectRequest request = new PutObjectRequest(bucketName, key, multipartFile.getInputStream(), metadata);
			PutObjectResult putObjectResult = awsService.getS3Client().putObject(request);
			System.out.println(putObjectResult.getETag());
			user.setProfileImageUrl(key);
			userRepository.save(user);
		} else {
			PutObjectRequest request = new PutObjectRequest(bucketName, user.getProfileImageUrl(),
					multipartFile.getInputStream(), metadata);
			PutObjectResult putObjectResult = awsService.getS3Client().putObject(request);
			System.out.println(putObjectResult.getETag());
		}
		return user.getProfileImageUrl();
	}

	public String getImage(String key) throws IOException {
		String bucketName = userImagesBucket;
		URL url = awsService.getS3Client().generatePresignedUrl(bucketName, key, DateUtils.next10Min());
		return Base64.getEncoder().encodeToString(IOUtils.toByteArray(url));
	}

	boolean isValidUrl(String url) throws MalformedURLException {
		try {
			new URL(url).toURI();
			return true;
		} catch (MalformedURLException e) {
			return false;
		} catch (URISyntaxException e) {
			return false;
		}
	}

	public long savePhoneNumber(long userId, String newPhonenumber) throws Exception {
		Optional<User> user = Optional.ofNullable(userRepository.findById(userId).orElseThrow(() -> new WorkruitException("User not found with Id: " + userId)));
		User existingUser = userRepository.findByMobile(newPhonenumber);
		if (existingUser != null) {
			throw new WorkruitException("User exists the same mobile: " + newPhonenumber);
		}
		String otp = sendOTP(newPhonenumber);
		user.get().setMobile(newPhonenumber);
		updateUserVerification(user.get(), otp);
		userRepository.save(user.get());

		return userId;
	}
}
