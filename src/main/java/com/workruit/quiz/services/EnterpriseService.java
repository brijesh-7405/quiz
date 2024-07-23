/**
 * 
 */
package com.workruit.quiz.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.workruit.quiz.configuration.FieldValidationException;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.ExceptionResponse.FieldError;
import com.workruit.quiz.controllers.dto.CategoryDTO;
import com.workruit.quiz.controllers.dto.CategoryListDTO;
import com.workruit.quiz.controllers.dto.EnterpriseDTO;
import com.workruit.quiz.controllers.dto.EnterpriseDetails;
import com.workruit.quiz.controllers.dto.EnterpriseDetailsDTO;
import com.workruit.quiz.controllers.dto.EnterpriseSubscriptionDetailsDTO;
import com.workruit.quiz.controllers.dto.QuizDTO;
import com.workruit.quiz.controllers.dto.SubscriptionDetailsDTO;
import com.workruit.quiz.persistence.entity.Category;
import com.workruit.quiz.persistence.entity.Enterprise;
import com.workruit.quiz.persistence.entity.Enterprise.EnterpriseStatus;
import com.workruit.quiz.persistence.entity.Enterprise.EnterpriseType;
import com.workruit.quiz.persistence.entity.Quiz;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;
import com.workruit.quiz.persistence.entity.QuizTopicList;
import com.workruit.quiz.persistence.entity.SubscriptionEnterprisePurchase;
import com.workruit.quiz.persistence.entity.repository.CategoryRepository;
import com.workruit.quiz.persistence.entity.repository.EnterpriseRepository;
import com.workruit.quiz.persistence.entity.repository.QuizCategoryListRepository;
import com.workruit.quiz.persistence.entity.repository.QuizRepository;
import com.workruit.quiz.persistence.entity.repository.QuizTopicListRepository;
import com.workruit.quiz.persistence.entity.repository.SubscriptionEnterprisePurchaseRepository;
import com.workruit.quiz.persistence.entity.repository.SubscriptionPlanRepository;
import com.workruit.quiz.persistence.entity.repository.UserQuizRepository;
import com.workruit.quiz.persistence.entity.repository.UserRepository;
import com.workruit.quiz.services.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Santosh
 *
 */
@Service
@Slf4j
public class EnterpriseService {
	private @Autowired EnterpriseRepository enterpriseRepository;
	private @Autowired ModelMapper modelMapper;
	private @Autowired QuizRepository quizRepository;
	private @Autowired QuizCategoryListRepository quizCategoryListRepository;
	private @Autowired QuizTopicListRepository quizTopicListRepository;
	private @Autowired CategoryRepository categoryRepository;
	private @Autowired AWSService awsService;
	@Value("${enterprise.images.bucket}")
	private String enterpriseImagesBucket;

	private @Autowired SubscriptionEnterprisePurchaseService subscriptionEnterprisePurchaseService;
	private @Autowired SubscriptionPlanRepository subscriptionPlanRepository;
	private @Autowired SubscriptionEnterprisePurchaseRepository subscriptionEnterprisePurchaseRepository;
	private @Autowired SubscriptionPlanFeatureMappingService subscriptionPlanFeatureMappingService;
	private @Autowired UserRepository userRepository;
	private @Autowired UserQuizRepository userQuizRepository;

	@Transactional
	public long save(EnterpriseDTO enterpriseDTO, long userId, boolean isUpdate) throws Exception {
		try {
			List<FieldError> fieldErrors = new ArrayList<>();
			Enterprise enterprise = modelMapper.map(enterpriseDTO, Enterprise.class);
			if (enterpriseDTO.getEnterpriseCode() == null || enterpriseDTO.getEnterpriseCode().equals("")) {
				enterprise.setEnterpriseCode(RandomStringUtils.randomAlphanumeric(8).toUpperCase());
			}
			enterprise.setEnterpriseType(enterpriseDTO.getEnterpriseType() != null
					? EnterpriseType.valueOf(enterpriseDTO.getEnterpriseType())
					: EnterpriseType.PUBLIC);
			if (StringUtils.isBlank(enterprise.getName())) {
				fieldErrors.add(new FieldError("name", "Name field cannot be empty/null"));
			}

			if (fieldErrors.size() > 0) {
				throw new FieldValidationException(fieldErrors);
			} else {
				enterprise = enterpriseRepository.save(enterprise);
				if (!isUpdate) {
					subscriptionEnterprisePurchaseService.assignSubscriptionForEnterprise(enterprise.getId(),
							subscriptionPlanRepository.findBySubscriptionName("Trial").getSubscriptionId(), userId);
				}
				return enterprise.getId();
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public Long count() {
		return enterpriseRepository.count();
	}

	public int listCount(long userId) {
		String accessCode = userRepository.findById(userId).get().getAccessCode();
		List<Long> enterpriseIds = new ArrayList<>();
		if (accessCode != null) {
			String[] accessCodes = accessCode.split(",");
			for (String code : accessCodes) {
				Enterprise e = enterpriseRepository.findByEnterpriseCodeAndEnterpriseType(code, EnterpriseType.PRIVATE);
				if (e != null) {
					enterpriseIds.add(e.getId());
				}
			}
		}

		return enterpriseIds.size() == 0 ? enterpriseRepository.getEnterprisesWithActiveQuizsCount(userId)
				: enterpriseRepository.getEnterprisesWithActiveQuizsCountPublicPrivate(enterpriseIds, userId);
	}

	public List<EnterpriseDetailsDTO> list(int page, int size, String sortBy, long userId) {
		try {
			String accessCode = userRepository.findById(userId).get().getAccessCode();
			List<Long> enterpriseIds = new ArrayList<>();
			if (accessCode != null) {
				String[] accessCodes = accessCode.split(",");
				for (String code : accessCodes) {
					Enterprise e = enterpriseRepository.findByEnterpriseCodeAndEnterpriseType(code,
							EnterpriseType.PRIVATE);
					if (e != null) {
						enterpriseIds.add(e.getId());
					}
				}
			}
			List<Object[]> output = enterpriseIds.size() == 0
					? enterpriseRepository.getEnterprisesWithActiveQuizs(userId, page * size, (page + 1) * size)
					: enterpriseRepository.getEnterprisesWithActiveQuizsPublicPrivate(userId, page * size,
							(page + 1) * size, enterpriseIds);
			List<Enterprise> result = new ArrayList<>();
			List<EnterpriseDetailsDTO> enterpriseDetailsDTOs = new ArrayList<>();
			for (Object[] objects : output) {
				Enterprise enterprise = new Enterprise();
				enterprise.setId(NumberUtils.toLong(objects[0].toString()));
				enterprise.setName(objects[1].toString());
				enterprise.setLocation(objects[2].toString());
				enterprise.setContactPersonName(objects[3].toString());
				enterprise.setContactEmail(objects[4].toString());
				enterprise.setAbout(objects[5].toString());
				enterprise.setCreatedDate((Timestamp) objects[6]);
				if (objects[7] != null && !"".equals(objects[7].toString())) {
					enterprise.setLogo(objects[7].toString());
				}
				if (objects[8] != null) {
					enterprise.setContactPhone(objects[8].toString());
				}
				if (objects[9] != null) {
					enterprise.setWebsite(objects[9].toString());
				}
				result.add(enterprise);
			}

			for (Enterprise enterprise : result) {
				List<Quiz> enterpriseQuizs = quizRepository.findByEnterpriseIdAndStatus(enterprise.getId(),
						QuizSubmitStatus.ACTIVE);
				List<Long> quizIds = enterpriseQuizs.stream().map(enterpriseQuiz -> enterpriseQuiz.getId())
						.collect(Collectors.toList());
				quizIds.removeAll(userQuizRepository.findByUserIdAndQuizIdIn(userId, quizIds).stream()
						.map(obj -> obj.getQuizId()).collect(Collectors.toList()));

				EnterpriseDetailsDTO enterpriseDetailsDTO = buildEnterpriseDetailsDTO(enterprise, quizIds);
				if (quizIds.size() > 0) {
					enterpriseDetailsDTOs.add(enterpriseDetailsDTO);
				}
			}
			return enterpriseDetailsDTOs;
		} catch (Exception e) {
			log.error("Error while listing enterprise", e);
			throw e;
		}
	}

	public List<EnterpriseDetailsDTO> listForAdmin(int pageNumber, int size) {
		try {
			PageRequest pageRequest = PageRequest.of(pageNumber, size, Sort.by(Direction.DESC, "createdDate"));
			List<Enterprise> list = enterpriseRepository.findAll(pageRequest).getContent();
			List<EnterpriseDetailsDTO> enterpriseDetailsDTOs = new ArrayList<>();
			for (Enterprise enterprise : list) {
				List<Quiz> enterpriseQuizs = quizRepository.findByEnterpriseId(enterprise.getId());
				List<Long> quizIds = enterpriseQuizs.stream().map(enterpriseQuiz -> enterpriseQuiz.getId())
						.collect(Collectors.toList());
				EnterpriseDetailsDTO enterpriseDetailsDTO = buildEnterpriseDetailsDTO(enterprise, quizIds);
				enterpriseDetailsDTO
						.setStatus(enterprise.getStatus() == EnterpriseStatus.VERIFIED ? "Verified" : "Not Verified");
				if (quizIds.size() > 0) {
					enterpriseDetailsDTOs.add(enterpriseDetailsDTO);
				}
			}
			return enterpriseDetailsDTOs;
		} catch (Exception e) {
			throw e;
		}
	}

	public void authorize(Long enterpriseId) throws WorkruitException {
		Optional<Enterprise> optional = enterpriseRepository.findById(enterpriseId);
		if (!optional.isPresent()) {
			throw new WorkruitException("Enterprise does not exist");
		}
		optional.get().setStatus(EnterpriseStatus.VERIFIED);
		enterpriseRepository.save(optional.get());
	}

	private EnterpriseDetailsDTO buildEnterpriseDetailsDTO(Enterprise enterprise, List<Long> quizIds) {
		List<QuizTopicList> quizTopicLists = quizTopicListRepository.findByQuizIdIn(quizIds);
		EnterpriseDetailsDTO enterpriseDetailsDTO = new EnterpriseDetailsDTO();
		enterpriseDetailsDTO.setId(enterprise.getId());
		enterpriseDetailsDTO.setName(enterprise.getName());
		enterpriseDetailsDTO.setNumberOfQuizs(quizIds.size());
		enterpriseDetailsDTO.setNumberOfTopics(
				(long) quizTopicLists.stream().map(x -> x.getTopicId()).collect(Collectors.toSet()).size());
		try{
			enterpriseDetailsDTO.setLogo(enterprise.getLogo() != null ? getImage(enterprise.getLogo()) : null);
		}catch (IOException e) {
			log.error(e.getMessage());
		}
		enterpriseDetailsDTO.setLogoKey(enterprise.getLogo());
		enterpriseDetailsDTO.setEnterpriseContactEmail(enterprise.getContactEmail());
		enterpriseDetailsDTO.setEnterpriseContactPhone(enterprise.getContactPhone());
		enterpriseDetailsDTO.setEnterpriseLocation(enterprise.getLocation());
		enterpriseDetailsDTO.setEnterprisePersonName(enterprise.getContactPersonName());
		enterpriseDetailsDTO.setEnterpriseWebsite(enterprise.getWebsite());
		enterpriseDetailsDTO.setAbout(enterprise.getAbout());
		enterpriseDetailsDTO.setEnterpriseCreatedDate(DateUtils.format(enterprise.getCreatedDate()));
		return enterpriseDetailsDTO;
	}

	public EnterpriseDetails getByEmail(String email) throws WorkruitException, IOException {
		Enterprise enterprise = enterpriseRepository.findByContactEmail(email);
		if (enterprise != null) {
			return buildDetails(enterprise);
		} else {
			throw new WorkruitException("Enterprise does not exist for this email");
		}
	}

	public EnterpriseSubscriptionDetailsDTO getEnterpriseSubscription(Long enterpriseId) throws Exception {
		SubscriptionEnterprisePurchase subscriptionEnterprisePurchase = subscriptionEnterprisePurchaseRepository
				.findByEnterpriseIdAndSubscriptionStatusAndSubscriptionPlanIdNotNull(enterpriseId, true);
		SubscriptionDetailsDTO suubscDetailsDTO = subscriptionPlanFeatureMappingService
				.getSubscriptionFeaturesBySubscriptionPlan(subscriptionEnterprisePurchase.getSubscriptionPlanId());
		Long count = (long) quizRepository.findAllByEnterpriseIdAndCreatedDateBetween(enterpriseId,
				new Date(subscriptionEnterprisePurchase.getSubscriptionPurchaseDate().getTime()),
				new Date(subscriptionEnterprisePurchase.getSubscriptionEndDate().getTime())).size();
		EnterpriseSubscriptionDetailsDTO enterpriseSubscriptionDetailsDTO = new EnterpriseSubscriptionDetailsDTO();
		enterpriseSubscriptionDetailsDTO.setSubscriptionDetails(suubscDetailsDTO);
		enterpriseSubscriptionDetailsDTO.setQuizLimitUsage(count);
		enterpriseSubscriptionDetailsDTO
				.setSubscriptionStartDate(subscriptionEnterprisePurchase.getSubscriptionPurchaseDate());
		enterpriseSubscriptionDetailsDTO
				.setSubscriptionEndDate(subscriptionEnterprisePurchase.getSubscriptionEndDate());
		return enterpriseSubscriptionDetailsDTO;
	}

	public EnterpriseDetails get(Long id) throws Exception {
		try {
			Optional<Enterprise> optional = enterpriseRepository.findById(id);
			if (optional.isPresent()) {
				Enterprise enterprise = optional.get();
				return buildDetails(enterprise);
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	private EnterpriseDetails buildDetails(Enterprise enterprise) throws IOException {
		List<Quiz> quizs = quizRepository.findByEnterpriseIdAndStatus(enterprise.getId(), QuizSubmitStatus.ACTIVE);
		EnterpriseDetails enterpriseDetails = new EnterpriseDetails();
		EnterpriseDTO enterpriseDTO = buildDTO(enterprise);
		enterpriseDetails.setEnterprise(enterpriseDTO);
		enterpriseDetails.setQuizs(quizs.stream().map(quiz -> {
			QuizDTO quizDTO = new QuizDTO();
			quizDTO.setName(quiz.getName());
			quizDTO.setId(quiz.getId());
			quizDTO.setEnterpriseId(quiz.getEnterpriseId());
			quizDTO.setLevel(quiz.getLevel());
			quizDTO.setExpiryDate(DateUtils.format(quiz.getExpiryDate()));
			quizDTO.setTargetAudience(quiz.getTargetAudience());
			quizDTO.setCategoryIds(quizCategoryListRepository.findByQuizId(quiz.getId()).stream()
					.map(categoryMapping -> categoryMapping.getCategoryId()).collect(Collectors.toList()));
			quizDTO.setTopicIds(quizTopicListRepository.findByQuizId(quiz.getId()).stream()
					.map(topicMapping -> topicMapping.getTopicId()).collect(Collectors.toList()));
			quizDTO.setCode(quiz.getCode());

			Map<CategoryDTO, List<CategoryDTO>> categoryMap = new HashMap<>();
			quizCategoryListRepository.findByQuizId(quiz.getId()).forEach(quizCategory -> {
				Category category = categoryRepository.findById(quizCategory.getCategoryId()).get();
				CategoryDTO categoryDTO = new CategoryDTO(category.getId(), category.getName());
				categoryMap.putIfAbsent(categoryDTO, new ArrayList<>());
				List<Long> quizTopicIds = quizTopicListRepository.findByQuizId(quiz.getId()).stream()
						.map(x -> x.getTopicId()).collect(Collectors.toList());
				List<Category> findByParentId = categoryRepository.findByParentId(category.getId());
				List<Long> topicIds = findByParentId.stream().map(x -> x.getId()).collect(Collectors.toList());
				quizTopicIds.retainAll(topicIds);
				categoryMap.put(categoryDTO, findByParentId.stream().filter(x -> quizTopicIds.contains(x.getId()))
						.map(y -> new CategoryDTO(y.getId(), y.getName())).collect(Collectors.toList()));
				quizDTO.setCategories(categoryMap.entrySet().stream().map(entry -> {
					CategoryListDTO categoryListDTO = new CategoryListDTO();
					categoryListDTO.setId(entry.getKey().getId());
					categoryListDTO.setName(entry.getKey().getName());
					categoryListDTO.setTopics(entry.getValue());
					return categoryListDTO;
				}).collect(Collectors.toList()));
			});
			return quizDTO;
		}).collect(Collectors.toList()));
		return enterpriseDetails;
	}

	public String saveImage(MultipartFile multipartFile, Long enterpriseId) throws IOException {
		String bucketName = enterpriseImagesBucket;
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(multipartFile.getContentType());
		String key = RandomUtils.nextLong(0, Long.MAX_VALUE) + "";
		Enterprise enterprise = enterpriseRepository.findById(enterpriseId).get();
		if (StringUtils.isBlank(enterprise.getLogo())) {
			PutObjectRequest request = new PutObjectRequest(bucketName, key, multipartFile.getInputStream(), metadata);
			PutObjectResult putObjectResult = awsService.getS3Client().putObject(request);
			System.out.println(putObjectResult.getETag());
			return key;
		} else {
			PutObjectRequest request = new PutObjectRequest(bucketName, enterprise.getLogo(),
					multipartFile.getInputStream(), metadata);
			PutObjectResult putObjectResult = awsService.getS3Client().putObject(request);
			System.out.println(putObjectResult.getETag());
			return enterprise.getLogo();
		}
	}

	public String getImage(String key) throws IOException {
		String bucketName = enterpriseImagesBucket;
		URL url = awsService.getS3Client().generatePresignedUrl(bucketName, key, DateUtils.next10Min());
		return Base64.getEncoder().encodeToString(IOUtils.toByteArray(url));
	}

	public EnterpriseDTO buildDTO(Enterprise enterprise) throws IOException {
		EnterpriseDTO enterpriseDTO = new EnterpriseDTO();
		enterpriseDTO.setId(enterprise.getId());
		enterpriseDTO.setAbout(enterprise.getAbout());
		enterpriseDTO.setContactEmail(enterprise.getContactEmail());
		enterpriseDTO.setContactPersonName(enterprise.getContactPersonName());
		enterpriseDTO.setLocation(enterprise.getLocation());
		enterpriseDTO.setName(enterprise.getName());
		if (StringUtils.isNotBlank(enterprise.getLogo())) {
			enterpriseDTO.setLogo(getImage(enterprise.getLogo()));
		}
		enterpriseDTO.setLogoKey(StringUtils.isNotBlank(enterprise.getLogo()) ? enterprise.getLogo() : null);
		enterpriseDTO.setWebsite(enterprise.getWebsite());
		enterpriseDTO.setContactPhone(enterprise.getContactPhone());
		enterpriseDTO.setEnterpriseCode(enterprise.getEnterpriseCode());
		enterpriseDTO.setEnterpriseType(enterprise.getEnterpriseType().name());
		return enterpriseDTO;
	}

}
