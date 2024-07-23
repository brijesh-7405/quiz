package com.workruit.quiz.services;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.CategoryDTO;
import com.workruit.quiz.controllers.dto.CategoryListDTO;
import com.workruit.quiz.controllers.dto.EnterpriseDetailsDTO;
import com.workruit.quiz.controllers.dto.QuizAnalyticsDTO;
import com.workruit.quiz.controllers.dto.QuizAnalyticsDataDTO;
import com.workruit.quiz.persistence.entity.Category;
import com.workruit.quiz.persistence.entity.Enterprise;
import com.workruit.quiz.persistence.entity.Quiz;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;
import com.workruit.quiz.persistence.entity.QuizAnalyticsData;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;
import com.workruit.quiz.persistence.entity.repository.CategoryRepository;
import com.workruit.quiz.persistence.entity.repository.EnterpriseRepository;
import com.workruit.quiz.persistence.entity.repository.QuestionRepository;
import com.workruit.quiz.persistence.entity.repository.QuizAnalyticsDataRepository;
import com.workruit.quiz.persistence.entity.repository.QuizCategoryListRepository;
import com.workruit.quiz.persistence.entity.repository.QuizRepository;
import com.workruit.quiz.persistence.entity.repository.QuizTopicListRepository;
import com.workruit.quiz.persistence.entity.repository.UserQuizRepository;
import com.workruit.quiz.services.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

@EnableScheduling
@Component
@Transactional
@Slf4j
public class QuizAnalyticsService {
	private @Autowired EnterpriseRepository enterpriseRepository;
	private @Autowired QuizRepository quizRepository;
	private @Autowired UserQuizRepository userQuizRepository;
	private @Autowired QuizAnalyticsDataRepository quizAnalyticsDataRepository;
	private @Autowired QuestionRepository questionRepository;
	private @Autowired UserQuizService userQuizService;
	private @Autowired ModelMapper modelMapper;
	private @Autowired QuizTopicListRepository quizTopicListRepository;
	private @Autowired QuizCategoryListRepository quizCategoryListRepository;
	private @Autowired CategoryRepository categoryRepository;
	private @Autowired AnalyticsService analyticsService;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Transactional
	@Scheduled(fixedDelay = 60000)
	public void schedule() {
		try {
			Date date = DateUtils.incrementDays(new Date(), -2);
			Date twoDays = DateUtils.incrementDays(new Date(), 2);
			List<Quiz> quizs = quizRepository.findAllByExpiryDateGreaterThanEqualAndExpiryDateLessThanEqual(date,
					twoDays);
			for (Quiz quiz : quizs) {
				if (quiz.getStatus() == QuizSubmitStatus.ACTIVE || quiz.getStatus() == QuizSubmitStatus.REVIEW
						|| quiz.getStatus() == QuizSubmitStatus.PENDING) {
					System.out.println(date + "," + twoDays);
					System.out.println(quiz.getId() + ":" + quiz.getExpiryDate());
					boolean expired = DateUtils.nextDayStartingTime(quiz.getExpiryDate()).getTime()
							- DateUtils.resetTime(new Date()).getTime() <= 0L;
					if (expired) {
						quiz.setStatus(QuizSubmitStatus.CLOSE);
						quizRepository.save(quiz);
						analyticsService.updateQuizActions(quiz.getId());

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Long count(String enterpriseEmail) throws WorkruitException {
		Enterprise enterprise = enterpriseRepository.findByContactEmail(enterpriseEmail);
		if (enterprise == null) {
			throw new WorkruitException("Enterprise not found");
		}
		return quizAnalyticsDataRepository.countByEnterpriseId(enterprise.getId());
	}
/// Need to add status expiry.
	public QuizAnalyticsDTO analyticsQuizData(Long quizId, Long enterpriseId) throws Exception {
		try {
			Quiz quiz = quizRepository.findByIdAndEnterpriseId(quizId, enterpriseId);
			if (quiz == null) {
				throw new WorkruitException("Quiz cannot be accessed by this enterprise");
			}
			PageRequest pageRequest = PageRequest.of(0, 1);
			QuizAnalyticsData quizAnalyticsData = quizAnalyticsDataRepository.findByQuizId(quizId);
			QuizAnalyticsDTO quizAnalyticsDTO = modelMapper.map(quizAnalyticsData, QuizAnalyticsDTO.class);
			EnterpriseDetailsDTO enterpriseDetailsDTO = modelMapper.map(
					enterpriseRepository.findById(quizAnalyticsData.getEnterpriseId()).get(),
					EnterpriseDetailsDTO.class);
			enterpriseDetailsDTO.setEnterpriseCreatedDate(DateUtils.format(enterpriseDetailsDTO.getCreatedDate()));
			quizAnalyticsDTO.setEnterprise(enterpriseDetailsDTO);
			quizAnalyticsDTO.setQuizCreatedDate(DateUtils.format(quizAnalyticsDTO.getCreatedDate()));
			quizAnalyticsDTO.setQuizExpiryDate(DateUtils.format(quizAnalyticsDTO.getExpiryDate()));
			quizAnalyticsDTO.setCode(quizRepository.findById(quizAnalyticsData.getQuizId()).get().getCode());
			quizAnalyticsDTO.setQuizTimeLimit(quiz.getQuizTimeLimit());
			quizAnalyticsDTO.setQuizStatus(quiz.getStatus());
			if (quizAnalyticsData.getCategories() != null) {
				TypeReference<List<CategoryListDTO>> typeRef = new TypeReference<List<CategoryListDTO>>() {
				};
				quizAnalyticsDTO.setCategories(objectMapper.readValue(quizAnalyticsData.getCategories(), typeRef));
			}
			return quizAnalyticsDTO;
		} catch (Exception e) {
			throw e;
		}
	}

	public Long countByEnterpriseId(Long enterpriseId) {
		return quizAnalyticsDataRepository.countByEnterpriseId(enterpriseId);
	}

	public List<QuizAnalyticsDTO> analyticsEnterpriseQuizData(Long enterpriseId, int page, int size) {
		try {
			ModelMapper modelMapper = new ModelMapper();
			Converter<String, List<CategoryListDTO>> converter = getCategoriesConverter();
			modelMapper.addConverter(converter);
			PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "postedDate"));
			List<QuizAnalyticsData> analyticsDatas = quizAnalyticsDataRepository.findByEnterpriseId(enterpriseId,
					pageRequest);
			List<QuizAnalyticsDTO> list = analyticsDatas.stream()
					.map(data -> modelMapper.map(data, QuizAnalyticsDTO.class)).collect(Collectors.toList());

			Enterprise enterprise = enterpriseRepository.findById(enterpriseId).get();
			list.stream().forEach(obj -> {
				obj.setQuizExpiryDate(DateUtils.format(obj.getExpiryDate()));
				obj.setQuizCreatedDate(DateUtils.format(obj.getCreatedDate()));
				obj.setEnterprise(modelMapper.map(enterprise, EnterpriseDetailsDTO.class));
				obj.getEnterprise().setEnterpriseCreatedDate(DateUtils.format(obj.getEnterprise().getCreatedDate()));
			});
			return list;
		} catch (Exception e) {
			throw e;
		}
	}

	private Converter<String, List<CategoryListDTO>> getCategoriesConverter() {
		Converter<String, List<CategoryListDTO>> converter = new AbstractConverter<String, List<CategoryListDTO>>() {
			protected List<CategoryListDTO> convert(String source) {
				if (source != null) {
					TypeReference<List<CategoryListDTO>> typeRef = new TypeReference<List<CategoryListDTO>>() {
					};
					try {
						return objectMapper.readValue(source, typeRef);
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};
		return converter;
	}

	public List<QuizAnalyticsDataDTO> analyticsData(int page, int size, String sort, String contactEmail)
			throws Exception {
		try {
			Enterprise enterprise = enterpriseRepository.findByContactEmail(contactEmail);
			if (enterprise == null) {
				throw new WorkruitException("Enterprise not found");
			}
			List<QuizAnalyticsData> quizAnalyticsDataPage = quizAnalyticsDataRepository
					.findByEnterpriseIdOrderByAnalyticsTimeDesc(enterprise.getId(), PageRequest.of(0, 1));
			if (CollectionUtils.isEmpty(quizAnalyticsDataPage)) {
				return new ArrayList<>();
			}
			ModelMapper modelMapper = new ModelMapper();
			Converter<String, List<CategoryListDTO>> converter = getCategoriesConverter();
			modelMapper.addConverter(converter);
			String[] sortFields = sort.split(":");
			PageRequest of = PageRequest.of(page, size, Sort.by(Direction.valueOf(sortFields[1]), sortFields[0]));
			List<QuizAnalyticsData> quizAnalyticsDatas = quizAnalyticsDataRepository
					.findByEnterpriseId(enterprise.getId(), of);
			List<QuizAnalyticsDataDTO> list = quizAnalyticsDatas.stream()
					.map(data -> modelMapper.map(data, QuizAnalyticsDataDTO.class)).collect(Collectors.toList());
			list.stream().forEach(obj -> {
				obj.setQuizExpiryDate(DateUtils.format(obj.getExpiryDate()));
				obj.setQuizPostedDate(DateUtils.format(obj.getPostedDate()));
				obj.setQuizActivatedDate(DateUtils.format(obj.getActivatedDate()));
				obj.setQuizReviewDate(DateUtils.format(obj.getReviewDate()));
				obj.setQuizClosedDate(DateUtils.format(obj.getClosedDate()));
				obj.setQuizRejectedeDate(DateUtils.format(obj.getRejectedDate()));
			});
			return list;
		} catch (Exception e) {
			throw e;
		}
	}

	@Transactional
	public void persist() {
		try {
			List<Enterprise> enterprises = enterpriseRepository.findAll();
			for (Enterprise enterprise : enterprises) {
				List<QuizAnalyticsData> quizAnalyticsDatas = new ArrayList<>();
				List<Quiz> quizs = quizRepository.findByEnterpriseId(enterprise.getId());
				for (Quiz quiz : quizs) {
					QuizAnalyticsData quizAnalyticsData = persistQuizAnalyticsQuiz(enterprise, quiz);
					quizAnalyticsDatas.add(quizAnalyticsData);
				}
				quizAnalyticsDataRepository.saveAll(quizAnalyticsDatas);
			}
		} catch (Exception e) {
			log.error("Error while saving analytics data", e);
		}
	}
	/*
	 * @PostConstruct public void init() { persistOne(17L); persistOne(15L); }
	 */

	@Transactional
	public void persistOne(Long quizId) {
		Quiz quiz = quizRepository.findById(quizId).get();
		Enterprise enterprise = enterpriseRepository.findById(quiz.getEnterpriseId()).get();
		QuizAnalyticsData quizAnalyticsData = persistQuizAnalyticsQuiz(enterprise, quiz);
		quizAnalyticsDataRepository.deleteByQuizId(quizId);
		quizAnalyticsDataRepository.save(quizAnalyticsData);
	}

	private QuizAnalyticsData persistQuizAnalyticsQuiz(Enterprise enterprise, Quiz quiz) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		log.info("Processing for quiz:{}", quiz.getId());
		List list = userQuizRepository.groupByQuizIdAndQuizStatus(quiz.getId());
		QuizAnalyticsData quizAnalyticsData = new QuizAnalyticsData();
		quizAnalyticsData.setAnalyticsTime(timestamp);
		quizAnalyticsData.setEnterpriseId(enterprise.getId());
		quizAnalyticsData.setEnterpriseName(enterprise.getName());
		quizAnalyticsData.setQuizId(quiz.getId());
		quizAnalyticsData.setQuizName(quiz.getName());
		quizAnalyticsData.setQuizStatus(quiz.getStatus());
		quizAnalyticsData.setActivatedDate(quiz.getActivatedDate());
		quizAnalyticsData.setRejectedDate(quiz.getRejectedDate());
		quizAnalyticsData.setReviewDate(quiz.getReviewDate());
		quizAnalyticsData.setClosedDate(quiz.getClosedDate());
		long timedout = 0;
		long totalCount = 0;
		for (Object obj : list) {
			Object[] array = (Object[]) obj;
			BigInteger count = (BigInteger) array[1];
			totalCount = totalCount + count.longValue();
			if (array[0].toString().equals(UserQuizStatus.IN_PROGRESS.name())) {
				quizAnalyticsData.setHowManyInprogressQuiz(count.longValue());
			} else if (array[0].toString().equals(UserQuizStatus.NOT_STARTED.name())) {
				quizAnalyticsData.setHowManyYetToTakeQuiz(count.longValue());
			} else if (array[0].toString().equals(UserQuizStatus.COMPLETED.name())) {
				long completedTimedout =  userQuizRepository.countByQuizIdAndQuizTimedOutAndStatus(quiz.getId(), true,UserQuizStatus.COMPLETED);
				timedout += completedTimedout;
				quizAnalyticsData.setHowManyCompletedQuiz(count.longValue() - completedTimedout);
				quizAnalyticsData.setHowManyTimedOutQuiz(timedout);
			} else if (array[0].toString().equals(UserQuizStatus.PENDING_REVIEW.name())) {
				long pendingTimedOut = userQuizRepository.countByQuizIdAndQuizTimedOutAndStatus(quiz.getId(), true,UserQuizStatus.PENDING_REVIEW);
				timedout += pendingTimedOut;
				quizAnalyticsData.setHowManyInReviewQuiz(count.longValue() - pendingTimedOut);
				quizAnalyticsData.setHowManyTimedOutQuiz(timedout);
			}
		}
		quizAnalyticsData.setHowManyTookQuiz(totalCount);
		quizAnalyticsData.setNumberOfQuestions(questionRepository.countByQuizId(quiz.getId()));
		quizAnalyticsData.setCategory(userQuizService.getCategoryNamesForQuiz(quiz.getId()));
		quizAnalyticsData.setTopic(userQuizService.getTopicNamesForQuiz(quiz.getId()));
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
			List<CategoryListDTO> collect = categoryMap.entrySet().stream().map(entry -> {
				CategoryListDTO categoryListDTO = new CategoryListDTO();
				categoryListDTO.setId(entry.getKey().getId());
				categoryListDTO.setName(entry.getKey().getName());
				categoryListDTO.setTopics(entry.getValue());
				return categoryListDTO;
			}).collect(Collectors.toList());
			try {
				quizAnalyticsData.setCategories(objectMapper.writeValueAsString(collect));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		});

		quizAnalyticsData.setExpiryDate(quiz.getExpiryDate());
		quizAnalyticsData.setPostedDate(quiz.getCreatedDate());
		quizAnalyticsData.setLevel(quiz.getLevel());
		quizAnalyticsData.setTargetAudience(quiz.getTargetAudience());
		quizAnalyticsData.setCode(quiz.getCode());
		quizAnalyticsData.setQuizTimeLimit(quiz.getQuizTimeLimit());
		return quizAnalyticsData;
	}
}
