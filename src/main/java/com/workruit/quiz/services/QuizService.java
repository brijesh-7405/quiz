/**
 * 
 */
package com.workruit.quiz.services;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.CategoryDTO;
import com.workruit.quiz.controllers.dto.EnterpriseQuizPreviewDTO;
import com.workruit.quiz.controllers.dto.QuestionPreviewDTO;
import com.workruit.quiz.controllers.dto.QuizDTO;
import com.workruit.quiz.controllers.dto.QuizDetailsDTO;
import com.workruit.quiz.controllers.dto.QuizQuestionComments;
import com.workruit.quiz.persistence.entity.Enterprise;
import com.workruit.quiz.persistence.entity.Question;
import com.workruit.quiz.persistence.entity.Quiz;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;
import com.workruit.quiz.persistence.entity.QuizCategoryList;
import com.workruit.quiz.persistence.entity.QuizTopicList;
import com.workruit.quiz.persistence.entity.User;
import com.workruit.quiz.persistence.entity.repository.CategoryRepository;
import com.workruit.quiz.persistence.entity.repository.EnterpriseRepository;
import com.workruit.quiz.persistence.entity.repository.QuestionRepository;
import com.workruit.quiz.persistence.entity.repository.QuizCategoryListRepository;
import com.workruit.quiz.persistence.entity.repository.QuizRepository;
import com.workruit.quiz.persistence.entity.repository.QuizTopicListRepository;
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
public class QuizService {

	private @Autowired QuizRepository quizRepository;
	private @Autowired QuizCategoryListRepository quizCategoryListRepository;
	private @Autowired QuizTopicListRepository quizTopicListRepository;
	private @Autowired CategoryRepository categoryRepository;
	private @Autowired EnterpriseRepository enterpriseRepository;
	private @Autowired UserQuizRepository userQuizRepository;
	private @Autowired QuestionRepository questionRepository;
	private @Autowired UserRepository userRepository;

	private @Autowired EnterpriseService enterpriseService;
	private @Autowired AnalyticsService analyticsService;
	private @Autowired SubscriptionLimitService subscriptionLimitService;

	@Transactional
	public long createQuiz(QuizDTO quizDTO, Long enterpriseId) throws Exception {
		try {
			boolean limitCrossed = subscriptionLimitService.checkQuizLimit(enterpriseId);
			if (!limitCrossed) {
				throw new WorkruitException("Maximum Quiz Limit reached");
			}
			Quiz quiz = new Quiz();
			quiz.setCreatedDate(new Timestamp(new Date().getTime()));
			quiz.setEnterpriseId(enterpriseId);
			setQuiz(quizDTO, quiz);
			analyticsService.updateQuizActions(quiz.getId());
			return quiz.getId();
		} catch (Exception e) {
			log.error("Error while saving the quiz", e);
			throw e;
		}
	}

	public List<QuizDTO> filter(Long enterpriseId, List<Long> categoryIds, Long userId) {
		try {
			if (enterpriseId != null) {
				if (categoryIds != null && categoryIds.size() > 0) {
					List<Quiz> quizs = quizRepository.findByEnterpriseIdAndStatus(enterpriseId,
							QuizSubmitStatus.ACTIVE);
					List<QuizDTO> collect = quizs.stream().map(quiz -> transform(quiz)).collect(Collectors.toList());
					List<QuizDTO> targetList = new ArrayList<>();
					for (QuizDTO quizDTO : collect) {
						quizDTO.setCategoryIds(quizCategoryListRepository.findByQuizId(quizDTO.getId()).stream()
								.map(category -> category.getCategoryId()).filter(id -> categoryIds.contains(id))
								.collect(Collectors.toList()));
						quizDTO.setTopicIds(quizTopicListRepository.findByQuizId(quizDTO.getId()).stream()
								.map(topic -> topic.getTopicId()).filter(id -> categoryIds.contains(id))
								.collect(Collectors.toList()));
						if ((quizDTO.getCategoryIds() != null && quizDTO.getCategoryIds().size() > 0)
								|| (quizDTO.getTopicIds() != null && quizDTO.getTopicIds().size() > 0)) {
							targetList.add(quizDTO);
						}
					}
					return targetList;
				} else {
					List<Quiz> quizs = quizRepository.findByEnterpriseIdAndStatus(enterpriseId,
							QuizSubmitStatus.ACTIVE);
					List<QuizDTO> collect = quizs.stream().map(quiz -> transform(quiz)).collect(Collectors.toList());
					for (QuizDTO quizDTO : collect) {
						quizDTO.setCategoryIds(quizCategoryListRepository.findByQuizId(quizDTO.getId()).stream()
								.map(category -> category.getCategoryId()).collect(Collectors.toList()));
						quizDTO.setTopicIds(quizTopicListRepository.findByQuizId(quizDTO.getId()).stream()
								.map(topic -> topic.getTopicId()).collect(Collectors.toList()));
					}
					return collect;
				}
			} else {
				if (categoryIds != null && categoryIds.size() > 0) {
					User user = userRepository.findById(userId).get();
					List<Quiz> quizs = null;
					if (StringUtils.isNotBlank(user.getAccessCode())) {
						quizs = quizRepository.findByStatusByEnterpriseCode(QuizSubmitStatus.ACTIVE.name(),
								user.getAccessCode());
					} else {
						quizs = quizRepository.findByStatusByEnterpriseTypePublic(QuizSubmitStatus.ACTIVE.name());
					}
					List<QuizDTO> collect = quizs.stream().map(quiz -> transform(quiz)).collect(Collectors.toList());
					for (QuizDTO quizDTO : collect) {
						quizDTO.setCategoryIds(quizCategoryListRepository.findByQuizId(quizDTO.getId()).stream()
								.map(category -> category.getCategoryId()).filter(id -> categoryIds.contains(id))
								.collect(Collectors.toList()));
						quizDTO.setTopicIds(quizTopicListRepository.findByQuizId(quizDTO.getId()).stream()
								.map(topic -> topic.getTopicId()).filter(id -> categoryIds.contains(id))
								.collect(Collectors.toList()));
					}
					return collect;
				} else {
					User user = userRepository.findById(userId).get();
					List<Quiz> quizs = null;
					if (StringUtils.isNotBlank(user.getAccessCode())) {
						quizs = quizRepository.findByStatusByEnterpriseCode(QuizSubmitStatus.ACTIVE.name(),
								user.getAccessCode());
					} else {
						quizs = quizRepository.findByStatusByEnterpriseTypePublic(QuizSubmitStatus.ACTIVE.name());
					}
					List<QuizDTO> collect = quizs.stream().map(quiz -> transform(quiz)).collect(Collectors.toList());
					for (QuizDTO quizDTO : collect) {
						quizDTO.setCategoryIds(quizCategoryListRepository.findByQuizId(quizDTO.getId()).stream()
								.map(category -> category.getCategoryId()).collect(Collectors.toList()));
						quizDTO.setTopicIds(quizTopicListRepository.findByQuizId(quizDTO.getId()).stream()
								.map(topic -> topic.getTopicId()).collect(Collectors.toList()));
					}
					return collect;
				}
			}
		} catch (Exception e) {
			log.error("Error while saving the quiz", e);
			throw e;
		}
	}

	@Transactional(readOnly = true)
	public List<QuizDTO> getTopQuizzes() {
		try {
			List list = userQuizRepository.groupByQuizId();
			List<QuizDTO> quizDTOs = new ArrayList<>();
			for (Object obj : list) {
				Object[] array = (Object[]) obj;
				BigInteger quizId = (BigInteger) array[0];
				Quiz quiz = quizRepository.findByIdAndStatus(quizId.longValue(), QuizSubmitStatus.ACTIVE).get();
				QuizDTO quizDTO = transform(quiz);
				quizDTOs.add(quizDTO);
			}
			return quizDTOs;
		} catch (Exception e) {
			log.error("Error while saving the quiz", e);
			throw e;
		}
	}

	private QuizDTO transform(Quiz quiz) {
		QuizDTO quizDTO = new QuizDTO();
		quizDTO.setName(quiz.getName());
		quizDTO.setId(quiz.getId());
		quizDTO.setEnterpriseId(quiz.getEnterpriseId());
		Enterprise enterprise = enterpriseRepository.findById(quiz.getEnterpriseId()).get();
		if (StringUtils.isNotBlank(enterprise.getLogo())) {
			try {
				URL url = new URL(enterprise.getLogo());
				quizDTO.setEnterpriseLogo(Base64.getEncoder().encodeToString(IOUtils.toByteArray(url)));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		quizDTO.setLevel(quiz.getLevel());
		return quizDTO;
	}

	private void setQuiz(QuizDTO quizDTO, Quiz quiz) throws ParseException, WorkruitException {
		quiz.setCode(quizDTO.getCode());
		if (!enterpriseRepository.findById(quizDTO.getEnterpriseId()).isPresent()) {
			throw new WorkruitException("Enterprise selected is not present in the system");
		}
		quiz.setEnterpriseId(quizDTO.getEnterpriseId());
		quiz.setLevel(quizDTO.getLevel());
		quiz.setTargetAudience(quizDTO.getTargetAudience());
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		quiz.setName(quizDTO.getName());
		if (quiz.getQuizTimeLimit() != null && quiz.getQuizTimeLimit().split(":").length == 3) {
			quiz.setQuizTimeLimit(quizDTO.getQuizTimeLimit());
		}
		Date expiryDate = new Date(dateFormat.parse(quizDTO.getExpiryDate()).getTime());
		quiz.setStatus(QuizSubmitStatus.PENDING);
		if (DateUtils.nextDayStartingTime(expiryDate).getTime() - DateUtils.resetTime(new Date()).getTime() <= 0L) {
			throw new WorkruitException("Expiry Date cannot be past than current date");
		}
		//quiz.setExpiryDate(DateUtils.getDateinUTC(expiryDate));
		quiz.setExpiryDate(expiryDate);

		subscriptionLimitService.checkQuizExpiry(quizDTO.getEnterpriseId(), new Timestamp(expiryDate.getTime()));
		if (quizDTO.getCategoryIds() != null
				&& categoryRepository.countByIdIn(quizDTO.getCategoryIds()) != quizDTO.getCategoryIds().size()) {
			throw new WorkruitException("Category/Topic ids not present in the system");
		}

		if (quizDTO.getTopicIds() != null
				&& categoryRepository.countByIdIn(quizDTO.getTopicIds()) != quizDTO.getTopicIds().size()) {
			throw new WorkruitException("Category/Topic ids not present in the system");
		}
		quiz.setUuid(RandomStringUtils.randomAlphanumeric(6));
		Quiz persistentQuiz = quizRepository.save(quiz);
		if (quiz.getId() != null) {
			quizCategoryListRepository.deleteByQuizId(quiz.getId());
			quizTopicListRepository.deleteByQuizId(quiz.getId());
		}
		quizCategoryListRepository.saveAll(quizDTO.getCategoryIds().stream()
				.map(catId -> new QuizCategoryList(catId, persistentQuiz.getId())).collect(Collectors.toList()));
		quizTopicListRepository.saveAll(quizDTO.getTopicIds().stream()
				.map(catId -> new QuizTopicList(catId, persistentQuiz.getId())).collect(Collectors.toList()));
	}

	@Transactional
	public void updateQuiz(QuizDTO quizDTO, long quizId) throws Exception {
		try {
			List<QuizSubmitStatus> quizStatuses = new ArrayList<>();
			quizStatuses.add(QuizSubmitStatus.PENDING);
			Quiz quiz = quizRepository.findByIdAndStatusIn(quizId, quizStatuses);
			if (quiz == null) {
				throw new WorkruitException("No Active Quiz for the given Id.");
			}
			setQuiz(quizDTO, quiz);
			analyticsService.updateQuizActions(quiz.getId());
		} catch (Exception e) {
			log.error("Error while saving the quiz", e);
			throw e;
		}
	}

	@Transactional
	public void updateQuizStatus(Long quizId, QuizSubmitStatus status) throws Exception {
		try {

			List<Question> questions = questionRepository.findByQuizId(quizId);
			if (questions == null || questions.size() == 0) {
				throw new WorkruitException("Quiz does not have any questions added yet");
			}
			Optional<Quiz> quiz = null;
			if (status == QuizSubmitStatus.ACTIVE) {
				quiz = quizRepository.findByIdAndStatus(quizId, QuizSubmitStatus.REVIEW);
				quiz.get().setActivatedDate(new Timestamp(new Date().getTime()));
			}
			if (status == QuizSubmitStatus.REJECT) {
				quiz = quizRepository.findByIdAndStatus(quizId, QuizSubmitStatus.REVIEW);
				quiz.get().setRejectedDate(new Timestamp(new Date().getTime()));
			}
			if (status == QuizSubmitStatus.REVIEW) {
				quiz = quizRepository.findByIdAndStatus(quizId, QuizSubmitStatus.PENDING);
				if (!quiz.isPresent()) {
					quiz = quizRepository.findByIdAndStatus(quizId, QuizSubmitStatus.REJECT);
				}
				quiz.get().setReviewDate(new Timestamp(new Date().getTime()));
			}
			if (status == QuizSubmitStatus.CLOSE) {
				quiz = quizRepository.findById(quizId);
				quiz.get().setClosedDate(new Timestamp(new Date().getTime()));
			}
			if (!quiz.isPresent()) {
				throw new WorkruitException("Quiz Id not present or not in right state to update");
			}
			quiz.get().setStatus(status);
			quizRepository.save(quiz.get());
			analyticsService.updateQuizActions(quizId);
		} catch (WorkruitException e) {
			throw e;
		}
	}

	public void updateQuizComments(Long quizId, QuizQuestionComments comments) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Optional<Quiz> optional = quizRepository.findById(quizId);
		if (optional.isPresent()) {
			optional.get().setComments(objectMapper.writeValueAsString(comments));
			quizRepository.save(optional.get());
		}
	}

	@Transactional
	public void deleteQuiz(long id) {
		try {
			quizCategoryListRepository.deleteByQuizId(id);
			quizTopicListRepository.deleteByQuizId(id);
			quizRepository.deleteById(id);
		} catch (Exception e) {
			log.error("Error while deleting the quiz", e);
			throw e;
		}
	}

	public List<QuizDTO> getTopQuizzes(List<Long> quizIds) {
		try {
			List list = userQuizRepository.groupByQuizId();
			List<QuizDTO> quizDTOs = new ArrayList<>();
			for (Object obj : list) {
				Object[] array = (Object[]) obj;
				BigInteger quizId = (BigInteger) array[0];
				Quiz quiz = quizRepository.findByIdAndStatus(quizId.longValue(), QuizSubmitStatus.ACTIVE).get();
				QuizDTO quizDTO = transform(quiz);
				if (!quizIds.contains(quizId.longValue())) {
					quizDTOs.add(quizDTO);
				}
			}
			return quizDTOs;
		} catch (Exception e) {
			log.error("Error while saving the quiz", e);
			throw e;
		}
	}

	public EnterpriseQuizPreviewDTO quizPreview(Long quizId) throws Exception {
		try {
			Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);
			if (!optionalQuiz.isPresent()) {
				throw new WorkruitException("Quiz id is not present");
			}
			Quiz quiz = optionalQuiz.get();
			Enterprise enterprise = enterpriseRepository.findById(quiz.getEnterpriseId()).get();
			List<QuizCategoryList> quizCategoryLists = quizCategoryListRepository.findByQuizId(quizId);
			List<QuizTopicList> quizTopicLists = quizTopicListRepository.findByQuizId(quizId);
			List<Question> questions = questionRepository.findByQuizId(quizId);
			ObjectMapper objectMapper = new ObjectMapper();
			List<QuestionPreviewDTO> questionPreviewDTOs = new ArrayList<>();
			for (Question question : questions) {
				ArrayNode arrayNode = objectMapper.readValue(question.getOptions(), ArrayNode.class);
				QuestionPreviewDTO questionPreviewDTO = new QuestionPreviewDTO();
				JsonNode jsonNode = objectMapper.readValue(question.getQuestion(), JsonNode.class);
				questionPreviewDTO.setQuestion(jsonNode.get("question").asText());
				questionPreviewDTO.setType(question.getQuestionType());
				questionPreviewDTO.setNumberOfOptions(arrayNode.size());
				questionPreviewDTOs.add(questionPreviewDTO);
			}
			QuizDetailsDTO quizDetailsDTO = new QuizDetailsDTO();
			quizDetailsDTO.setCode(quiz.getCode());
			quizDetailsDTO.setExpiryDate(DateUtils.format(quiz.getExpiryDate()));
			quizDetailsDTO.setLevel(quiz.getLevel());
			quizDetailsDTO.setName(quiz.getName());
			quizDetailsDTO.setTargetAudience(quiz.getTargetAudience());
			quizDetailsDTO.setCategories(quizCategoryLists.stream()
					.map(category -> categoryRepository.findById(category.getCategoryId()).get())
					.map(entity -> CategoryDTO.builder().name(entity.getName()).id(entity.getId()).build())
					.collect(Collectors.toList()));
			quizDetailsDTO.setTopics(
					quizTopicLists.stream().map(topic -> categoryRepository.findById(topic.getTopicId()).get())
							.map(entity -> CategoryDTO.builder().name(entity.getName()).id(entity.getId()).build())
							.collect(Collectors.toList()));
			quizDetailsDTO.setQuizTimeLimit(quiz.getQuizTimeLimit());
			EnterpriseQuizPreviewDTO enterpriseQuizPreviewDTO = EnterpriseQuizPreviewDTO.builder()
					.questions(questionPreviewDTOs).enterprise(enterpriseService.buildDTO(enterprise))
					.quiz(quizDetailsDTO).build();
			return enterpriseQuizPreviewDTO;
		} catch (WorkruitException we) {
			throw we;
		} catch (Exception e) {
			throw e;
		}
	}
}
