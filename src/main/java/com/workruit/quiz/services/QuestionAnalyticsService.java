/**
 * 
 */
package com.workruit.quiz.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.constants.AnswerOption;
import com.workruit.quiz.controllers.dto.AnswerDTO;
import com.workruit.quiz.controllers.dto.QuestionAnalyticsDataDTO;
import com.workruit.quiz.controllers.dto.QuestionObjectDTO;
import com.workruit.quiz.controllers.dto.UserQuestionAnalyticsDataDTO;
import com.workruit.quiz.controllers.dto.UserQuestionAnalyticsDataDTO.AnswerStatus;
import com.workruit.quiz.persistence.entity.*;
import com.workruit.quiz.persistence.entity.Question.QuestionType;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;
import com.workruit.quiz.persistence.entity.repository.*;
import com.workruit.quiz.services.utils.DateUtils;
import com.workruit.quiz.services.utils.PercentageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Santosh
 *
 */
@EnableScheduling
@Component
@Transactional
@Slf4j
public class QuestionAnalyticsService {
	private @Autowired UserQuizRepository userQuizRepository;
	private @Autowired QuestionRepository questionRepository;
	private @Autowired AnswerRepository answerRepository;
	private @Autowired ModelMapper modelMapper;
	private @Autowired QuestionAnalyticsDataRepository questionAnalyticsDataRepository;
	private @Autowired UserRepository userRepository;
	private @Autowired QuizRepository quizRepository;
	private @Autowired UserQuizAnswersRepository userQuizAnswersRepository;
	private @Autowired UserAnswerAnalyticsDataRepository userAnswerAnalyticsDataRepository;
	private @Autowired QuestionAnswerService questionAnswerService;

	ObjectMapper objectMapper = new ObjectMapper();

	public void schedule() {
		try {
			persist();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Long count(Long quizId, Long enterpriseId) throws WorkruitException {
		Quiz quiz = quizRepository.findByIdAndEnterpriseId(quizId, enterpriseId);
		if (quiz == null) {
			throw new WorkruitException("Quiz cannot be accessedby by this enterprise");
		}
		return questionRepository.countByQuizId(quizId);
	}

	public List<QuestionAnalyticsDataDTO> analyticsQuestionsData(Long quizId, int page, int size) {
		try {
			final ObjectMapper objectMapper = new ObjectMapper();
			PageRequest of = PageRequest.of(page, size, Sort.by("questionId"));
			List<QuestionAnalyticsData> questionAnalyticsDatas = questionAnalyticsDataRepository.findByQuizId(quizId,
					of);
			if (questionAnalyticsDatas != null && questionAnalyticsDatas.size() > 0) {
				return questionAnalyticsDatas.stream()
						.map(data -> modelMapper.map(data, QuestionAnalyticsDataDTO.class)).collect(Collectors.toList())
						.stream().map(obj -> {
							try {
								QuestionObjectDTO questionObjectDTO = objectMapper.readValue(obj.getQuestion().toString(), QuestionObjectDTO.class);
								if(questionObjectDTO.getExplanationImage() != null){
									questionObjectDTO.setExplanationImage(questionAnswerService.getImage(questionObjectDTO.getExplanationImage()));
								}
								obj.setQuestion(objectMapper.convertValue(questionObjectDTO, JsonNode.class));
								obj.setAnswer(objectMapper.readValue(obj.getAnswer().toString(), JsonNode.class));
							} catch (JsonMappingException e) {
								e.printStackTrace();
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							return obj;
						}).collect(Collectors.toList());
			} else {
				of = PageRequest.of(page, size, Sort.by("id"));
				List<Question> questions = questionRepository.findByQuizId(quizId, of);
				List<QuestionAnalyticsDataDTO> dtos = new ArrayList<>();
				questions.forEach(question -> {
					QuestionAnalyticsDataDTO newObject = new QuestionAnalyticsDataDTO();
					try {
						QuestionObjectDTO questionObjectDTO = objectMapper.readValue(question.getQuestion().toString(), QuestionObjectDTO.class);
						if(questionObjectDTO.getExplanationImage() != null){
							questionObjectDTO.setExplanationImage(questionAnswerService.getImage(questionObjectDTO.getExplanationImage()));
						}
						newObject.setQuestion(objectMapper.convertValue(questionObjectDTO, JsonNode.class));
						Answer answer = answerRepository.findByQuestionId(question.getId());
						newObject.setAnswer(objectMapper.readValue(answer.getOption().toString(), JsonNode.class));
						newObject.setQuestionId(question.getId());
						newObject.setQuestionType(question.getQuestionType());
						newObject.setQuizId(quizId);
						dtos.add(newObject);
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
				return dtos;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public int questionAnalyticsCount(Long quizId, Long questionId, AnswerStatus answerStatus, Long enterpriseId)
			throws WorkruitException {
		Quiz quiz = quizRepository.findByIdAndEnterpriseId(quizId, enterpriseId);
		if (quiz == null) {
			throw new WorkruitException("Quiz cannot be access by this enterprise");
		}

		return userAnswerAnalyticsDataRepository.countByQuizIdAndQuestionIdAndAnswerStatus(quizId, questionId,
				answerStatus);
	}

	public List<UserQuestionAnalyticsDataDTO> questionAnalyticsByPagination(Long quizId, Long questionId,
			AnswerStatus answerStatus, int page, int size) throws JsonMappingException, JsonProcessingException {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Direction.DESC, "createdDate"));
		List<UserAnswerAnalyticsData> userAnswerAnalyticsDatas = userAnswerAnalyticsDataRepository
				.findByQuizIdAndQuestionIdAndAnswerStatus(quizId, questionId, answerStatus, pageable);
		List<UserQuestionAnalyticsDataDTO> userQuestionAnalyticsDataDTOs = new ArrayList<>();
		List<QuizSubmitStatus> listOfStatuses = new ArrayList<>();
		listOfStatuses.add(QuizSubmitStatus.ACTIVE);
		listOfStatuses.add(QuizSubmitStatus.CLOSE);
		List<UserQuizStatus> status = new ArrayList<>();
		status.add(UserQuizStatus.COMPLETED);
		status.add(UserQuizStatus.PENDING_REVIEW);

		for (UserAnswerAnalyticsData userAnswerAnalyticsData : userAnswerAnalyticsDatas) {
			Quiz quiz = quizRepository.findByIdAndStatusIn(userAnswerAnalyticsData.getQuizId(), listOfStatuses);
			UserQuiz userQuiz = userQuizRepository.findById(userAnswerAnalyticsData.getUserQuizId()).get();
			Optional<Question> question=questionRepository.findById(questionId);
			User user = userRepository.findById(userAnswerAnalyticsData.getUserId()).get();
			UserQuestionAnalyticsDataDTO userQuestionAnalyticsDataDTO = new UserQuestionAnalyticsDataDTO();
			userQuestionAnalyticsDataDTO.setUserQuizId(userAnswerAnalyticsData.getUserQuizId());
			userQuestionAnalyticsDataDTO.setAnswerStatus(answerStatus);
			userQuestionAnalyticsDataDTO.setEmail(user.getPrimaryEmail());
			userQuestionAnalyticsDataDTO.setFirstName(user.getFirstName());
			userQuestionAnalyticsDataDTO.setLastName(user.getLastName());
			userQuestionAnalyticsDataDTO.setMobile(user.getMobile());
			userQuestionAnalyticsDataDTO.setQuestionId(questionId);
			userQuestionAnalyticsDataDTO.setQuizId(userAnswerAnalyticsData.getQuizId());
			userQuestionAnalyticsDataDTO.setQuizName(quiz.getName());
			userQuestionAnalyticsDataDTO.setQuizStartedDate(DateUtils.format(userQuiz.getCreatedDate()));
			UserQuizStatusContent userOutputList = objectMapper.readValue(userQuiz.getResultContent(),
					UserQuizStatusContent.class);
			int likeDislikeCount = (int) userOutputList.getLikeDislikeCount();
			int totalNumberOfQuestions = userOutputList.getAnswersStatus().size() - likeDislikeCount;
			long correctCount = userOutputList.getAnswersStatus().entrySet().stream()
					.filter(entry -> entry.getValue() == Boolean.TRUE).count();
			long inCorrectCount = userOutputList.getAnswersStatus().entrySet().stream()
					.filter(entry -> entry.getValue() == Boolean.FALSE).count();
			userQuestionAnalyticsDataDTO.setCorrectPercentage(
					(int) Math.round(PercentageUtils.getPercentageInDouble(correctCount, totalNumberOfQuestions)));
			userQuestionAnalyticsDataDTO.setInCorrectPercentage(
					(int) Math.round(PercentageUtils.getPercentageInDouble(inCorrectCount, totalNumberOfQuestions)));
			Boolean output = userOutputList.getAnswersStatus().get(questionId);
			if (AnswerStatus.CORRECT == answerStatus && output != null && output.booleanValue()) {
				userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
			}
			if (AnswerStatus.INCORRECT == answerStatus && output != null && !output.booleanValue()) {
				userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
			}
			if (AnswerStatus.NOT_ANSWERED == answerStatus && output != null && !output.booleanValue()) {
				userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
			}
			if (AnswerStatus.LIKE == answerStatus) {
				userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
			}
			if (AnswerStatus.DISLIKE == answerStatus) {
				userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
			}
			if(AnswerStatus.NOT_ANSWERED == answerStatus && question.isPresent() && question.get().getQuestionType() == QuestionType.LIKE_OR_DISLIKE) {
				userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
			}
			if (AnswerStatus.INREVIEW == answerStatus && output == null) {
				UserQuizAnswers userQuizAnswers = userQuizAnswersRepository.findByUserQuizIdAndUserIdAndQuestionId(
						userQuiz.getId(), userAnswerAnalyticsData.getUserId(), questionId);
				JsonNode node = objectMapper.readValue(userQuizAnswers.getOption(), JsonNode.class);
				ArrayNode array = (ArrayNode) node.get("options");
				userQuestionAnalyticsDataDTO.setUserAnswer(array.get(0).get("option").asText());
				userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
			}
		}
		return userQuestionAnalyticsDataDTOs;
	}

	public List<UserQuestionAnalyticsDataDTO> questionAnalytics(Long quizId, Long questionId, AnswerStatus answerStatus,
			int page, int size) throws Exception {
		try {
			// Get the users who took quiz with that question
			Optional<Question> question = questionRepository.findById(questionId);
			if (!question.isPresent()) {
				throw new WorkruitException("Question Id not present in the System");
			}
			List<UserQuizStatus> status = new ArrayList<>();
			status.add(UserQuizStatus.COMPLETED);
			status.add(UserQuizStatus.PENDING_REVIEW);
			List<UserQuiz> userQuizs = userQuizRepository.findByQuizIdAndStatusIn(quizId, status);
			List<UserQuestionAnalyticsDataDTO> userQuestionAnalyticsDataDTOs = new ArrayList<>();
			ObjectMapper objectMapper = new ObjectMapper();
			List<QuizSubmitStatus> listOfStatuses = new ArrayList<>();
			listOfStatuses.add(QuizSubmitStatus.ACTIVE);
			listOfStatuses.add(QuizSubmitStatus.CLOSE);
			for (UserQuiz userQuiz : userQuizs) {
				Quiz quiz = quizRepository.findByIdAndStatusIn(userQuiz.getQuizId(), listOfStatuses);
				Long userId = userQuiz.getUserId();
				User user = userRepository.findById(userId).get();
				UserQuestionAnalyticsDataDTO userQuestionAnalyticsDataDTO = new UserQuestionAnalyticsDataDTO();
				userQuestionAnalyticsDataDTO.setUserQuizId(userQuiz.getId());
				userQuestionAnalyticsDataDTO.setAnswerStatus(answerStatus);
				userQuestionAnalyticsDataDTO.setEmail(user.getPrimaryEmail());
				userQuestionAnalyticsDataDTO.setFirstName(user.getFirstName());
				userQuestionAnalyticsDataDTO.setLastName(user.getLastName());
				userQuestionAnalyticsDataDTO.setMobile(user.getMobile());
				userQuestionAnalyticsDataDTO.setQuestionId(questionId);
				userQuestionAnalyticsDataDTO.setQuizId(question.get().getQuizId());
				userQuestionAnalyticsDataDTO.setQuizName(quiz.getName());
				userQuestionAnalyticsDataDTO.setQuizStartedDate(DateUtils.format(userQuiz.getCreatedDate()));
				UserQuizStatusContent userOutputList = objectMapper.readValue(userQuiz.getResultContent(),
						UserQuizStatusContent.class);
				int likeDislikeCount = (int) userOutputList.getLikeDislikeCount();
				int totalNumberOfQuestions = userOutputList.getAnswersStatus().size() - likeDislikeCount;
				long correctCount = userOutputList.getAnswersStatus().entrySet().stream()
						.filter(entry -> entry.getValue() == Boolean.TRUE).count();
				long inCorrectCount = userOutputList.getAnswersStatus().entrySet().stream()
						.filter(entry -> entry.getValue() == Boolean.FALSE).count();
				userQuestionAnalyticsDataDTO.setCorrectPercentage(
						(int) Math.round(PercentageUtils.getPercentageInDouble(correctCount, totalNumberOfQuestions)));
				userQuestionAnalyticsDataDTO.setInCorrectPercentage((int) Math
						.round(PercentageUtils.getPercentageInDouble(inCorrectCount, totalNumberOfQuestions)));
				Boolean output = userOutputList.getAnswersStatus().get(questionId);
				if (AnswerStatus.CORRECT == answerStatus && output != null && output.booleanValue()) {
					userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
				}
				if (AnswerStatus.INCORRECT == answerStatus && output != null && !output.booleanValue()) {
					userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
				}
				if (AnswerStatus.INREVIEW == answerStatus && output == null) {
					UserQuizAnswers userQuizAnswers = userQuizAnswersRepository
							.findByUserQuizIdAndUserIdAndQuestionId(userQuiz.getId(), userId, questionId);
					JsonNode node = objectMapper.readValue(userQuizAnswers.getOption(), JsonNode.class);
					ArrayNode array = (ArrayNode) node.get("options");
					userQuestionAnalyticsDataDTO.setUserAnswer(array.get(0).get("option").asText());
					userQuestionAnalyticsDataDTOs.add(userQuestionAnalyticsDataDTO);
				}
			}
			return userQuestionAnalyticsDataDTOs;
		} catch (Exception e) {
			throw e;
		}
	}



	public void persistOne(Long quizId) throws Exception {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		List<UserQuizStatus> results = new ArrayList<>();
		results.add(UserQuizStatus.COMPLETED);
		results.add(UserQuizStatus.PENDING_REVIEW);
		List<UserQuiz> userQuizs = userQuizRepository.findByQuizIdAndStatusIn(quizId, results);
		Map<Long, Long> correctMap = new HashMap<>();
		Map<Long, Long> incorrectMap = new HashMap<>();
		Map<Long, Long> inReviewMap = new HashMap<>();
		Map<Long, Long> unawnseredMap = new HashMap<>();
		Map<Long, Long> likedMap = new HashMap<>();
		Map<Long, Long> disLikedMap = new HashMap<>();


		for (UserQuiz userQuiz : userQuizs) {
			String result = userQuiz.getResultContent();
			if (result == null) {
				continue;
			}
			UserQuizStatusContent userQuizStatusContent = objectMapper.readValue(result, UserQuizStatusContent.class);
			Map<Long, Boolean> answerMap = userQuizStatusContent.getAnswersStatus();
			if (answerMap == null) {
				continue;
			}
			answerMap.entrySet().stream().forEach(entry -> {
				Long questionId = entry.getKey();
				Boolean answerStatus = entry.getValue();
				Question questionInfo = questionRepository.findById(questionId).get();
				UserAnswerAnalyticsData userAnswerAnalyticsData = userAnswerAnalyticsDataRepository
						.findByUserIdAndQuizIdAndQuestionId(userQuiz.getUserId(), quizId, questionId);
				if (userAnswerAnalyticsData == null) {
					userAnswerAnalyticsData = new UserAnswerAnalyticsData();
					userAnswerAnalyticsData.setUserId(userQuiz.getUserId());
					userAnswerAnalyticsData.setQuizId(quizId);
					userAnswerAnalyticsData.setQuestionId(questionId);
					userAnswerAnalyticsData.setUserQuizId(userQuiz.getId());
				}
				if (answerStatus != null) {
					if (answerStatus.booleanValue()) {
						if (correctMap.get(questionId) == null) {
							correctMap.put(questionId, 0L);
						}
						correctMap.put(questionId, correctMap.get(questionId) + 1);
						userAnswerAnalyticsData.setAnswerStatus(AnswerStatus.CORRECT);
					} else {
						UserQuizAnswers userAnswer = userQuizAnswersRepository.findByUserQuizIdAndUserIdAndQuestionId(
								userQuiz.getId(), userQuiz.getUserId(), questionId);
						if (userAnswer == null || StringUtils.isBlank(userAnswer.getOption())) {
							if (unawnseredMap.get(questionId) == null) {
								unawnseredMap.put(questionId, 0L);
							}
							unawnseredMap.put(questionId, unawnseredMap.get(questionId) + 1);
							userAnswerAnalyticsData.setAnswerStatus(AnswerStatus.NOT_ANSWERED);
						} else {
							if (incorrectMap.get(questionId) == null) {
								incorrectMap.put(questionId, 0L);
							}
							incorrectMap.put(questionId, incorrectMap.get(questionId) + 1);
							userAnswerAnalyticsData.setAnswerStatus(AnswerStatus.INCORRECT);
						}
					}
				} else {
					if(questionInfo != null && questionInfo.getQuestionType() == QuestionType.LIKE_OR_DISLIKE) {
						UserQuizAnswers userAnswer = userQuizAnswersRepository.findByUserQuizIdAndUserIdAndQuestionId(
								userQuiz.getId(), userQuiz.getUserId(), questionId);

						if (userAnswer == null || StringUtils.isBlank(userAnswer.getOption())) {
							if (unawnseredMap.get(questionId) == null) {
								unawnseredMap.put(questionId, 0L);
							}
							unawnseredMap.put(questionId, unawnseredMap.get(questionId) + 1);
							userAnswerAnalyticsData.setAnswerStatus(AnswerStatus.NOT_ANSWERED);
						}else {
							if(userAnswer.getOption() != null) {
								AnswerDTO userOutput = null;
								try {
									userOutput = objectMapper.readValue(userAnswer.getOption(), AnswerDTO.class);
								} catch (JsonProcessingException e) {
									throw new RuntimeException(e);
								}
								Object userAnswerOption = userOutput.getOptions().get(0).getOption();
								if(userAnswerOption.toString().equalsIgnoreCase(AnswerOption.LIKE.toString())){
									if(likedMap.get(questionId) == null){
										likedMap.put(questionId, 0L);
									}
									likedMap.put(questionId, likedMap.get(questionId) + 1);
									userAnswerAnalyticsData.setAnswerStatus(AnswerStatus.LIKE);
								} else if(userAnswerOption.toString().equalsIgnoreCase(AnswerOption.DISLIKE.toString())) {
									if(disLikedMap.get(questionId) == null){
										disLikedMap.put(questionId, 0L);
									}
									disLikedMap.put(questionId, disLikedMap.get(questionId) + 1);
									userAnswerAnalyticsData.setAnswerStatus(AnswerStatus.DISLIKE);
								}
							}
						}
					}else {
						if (inReviewMap.get(questionId) == null) {
							inReviewMap.put(questionId, 0L);
						}
						inReviewMap.put(questionId, inReviewMap.get(questionId) + 1);
						userAnswerAnalyticsData.setAnswerStatus(AnswerStatus.INREVIEW);
					}
				}
				userAnswerAnalyticsDataRepository.save(userAnswerAnalyticsData);
			});
		}

		long totalRecords = questionRepository.countByQuizId(quizId);
		int pages = (int) (totalRecords / 20);
		List<QuestionAnalyticsData> list = new ArrayList<>();
		for (int i = 0; i < pages + 1; i++) {
			Pageable page = PageRequest.of(i, 20);
			List<Question> questions = questionRepository.findByQuizId(quizId, page);
			questions.stream()
					.filter(question -> correctMap.get(question.getId()) != null
							|| incorrectMap.get(question.getId()) != null || inReviewMap.get(question.getId()) != null
							|| unawnseredMap.get(question.getId()) != null || likedMap.get(question.getId()) != null || disLikedMap.get(question.getId()) != null)
					.forEach(question -> {
						QuestionAnalyticsData questionAnalyticsData = new QuestionAnalyticsData();
						String q = question.getQuestion();
						QuestionType type = question.getQuestionType();
						Long correctCount = correctMap.getOrDefault(question.getId(), 0L);
						Long inCorrectCount = incorrectMap.getOrDefault(question.getId(), 0L);
						Long inReviewCount = inReviewMap.getOrDefault(question.getId(), 0L);
						Long unAnsweredCount = unawnseredMap.getOrDefault(question.getId(), 0L);
						Long likeCount = likedMap.getOrDefault(question.getId(), 0L);
						Long dislikeCount = disLikedMap.getOrDefault(question.getId(), 0L);
						questionAnalyticsData.setCorrectCount(correctCount);
						questionAnalyticsData.setInCorrectCount(inCorrectCount);
						questionAnalyticsData.setInReviewCount(inReviewCount);
						questionAnalyticsData.setUnAnsweredCount(unAnsweredCount);
						questionAnalyticsData.setLikeCount(likeCount);
						questionAnalyticsData.setDislikeCount(dislikeCount);
						questionAnalyticsData.setQuestionId(question.getId());
						questionAnalyticsData.setQuestionType(type);
						questionAnalyticsData.setQuestion(q);
						questionAnalyticsData.setQuizId(question.getQuizId());

						Answer findByQuestionId = answerRepository.findByQuestionId(question.getId());
						if (findByQuestionId != null) {
							questionAnalyticsData.setAnswer(findByQuestionId.getOption());
						}
						questionAnalyticsData.setAnalyticsTime(timestamp);
						list.add(questionAnalyticsData);
					});
		}
		questionAnalyticsDataRepository.deleteByQuizId(quizId);
		questionAnalyticsDataRepository.saveAll(list);
	}

	@Transactional
	private void persist() throws Exception {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		List<UserQuizStatus> results = new ArrayList<>();
		results.add(UserQuizStatus.COMPLETED);
		results.add(UserQuizStatus.PENDING_REVIEW);
		List<UserQuiz> userQuizs = userQuizRepository.findByStatusIn(results);
		Map<Long, Long> correctMap = new HashMap<>();
		Map<Long, Long> incorrectMap = new HashMap<>();
		Map<Long, Long> inReviewMap = new HashMap<>();
		Map<Long, Long> unawnseredMap = new HashMap<>();
		Map<Long, Long> likedMap = new HashMap<>();
		Map<Long, Long> disLikedMap = new HashMap<>();

		for (UserQuiz userQuiz : userQuizs) {
			String result = userQuiz.getResultContent();
			if (result == null) {
				continue;
			}
			UserQuizStatusContent userQuizStatusContent = objectMapper.readValue(result, UserQuizStatusContent.class);
			Map<Long, Boolean> answerMap = userQuizStatusContent.getAnswersStatus();
			if (answerMap == null) {
				continue;
			}
			answerMap.entrySet().stream().forEach(entry -> {
				Long questionId = entry.getKey();
				Boolean answerStatus = entry.getValue();
				Question questionInfo = questionRepository.findById(questionId).get();

				if (answerStatus != null) {
					if (answerStatus.booleanValue()) {
						if (correctMap.get(questionId) == null) {
							correctMap.put(questionId, 0L);
						}
						correctMap.put(questionId, correctMap.get(questionId) + 1);
					} else {
						UserQuizAnswers userAnswer = userQuizAnswersRepository.findByUserQuizIdAndUserIdAndQuestionId(
								userQuiz.getId(), userQuiz.getUserId(), questionId);
						if (userAnswer == null || StringUtils.isBlank(userAnswer.getOption())) {
							if (unawnseredMap.get(questionId) == null) {
								unawnseredMap.put(questionId, 0L);
							}
							unawnseredMap.put(questionId, unawnseredMap.get(questionId) + 1);
						} else {
							if (incorrectMap.get(questionId) == null) {
								incorrectMap.put(questionId, 0L);
							}
							incorrectMap.put(questionId, incorrectMap.get(questionId) + 1);
						}
					}
				} else {
					if(questionInfo != null && questionInfo.getQuestionType() == QuestionType.LIKE_OR_DISLIKE) {
						UserQuizAnswers userAnswer = userQuizAnswersRepository.findByUserQuizIdAndUserIdAndQuestionId(
								userQuiz.getId(), userQuiz.getUserId(), questionId);

						if (userAnswer == null || StringUtils.isBlank(userAnswer.getOption())) {
							if (unawnseredMap.get(questionId) == null) {
								unawnseredMap.put(questionId, 0L);
							}
							unawnseredMap.put(questionId, unawnseredMap.get(questionId) + 1);
						}else {
							if(userAnswer.getOption() != null) {
								AnswerDTO userOutput = null;
								try {
									userOutput = objectMapper.readValue(userAnswer.getOption(), AnswerDTO.class);
								} catch (JsonProcessingException e) {
									throw new RuntimeException(e);
								}
								Object userAnswerOption = userOutput.getOptions().get(0).getOption();
								if(userAnswerOption.toString().equalsIgnoreCase(AnswerOption.LIKE.toString())){
									if(likedMap.get(questionId) == null){
										likedMap.put(questionId, 0L);
									}
									likedMap.put(questionId, likedMap.get(questionId) + 1);
								} else if(userAnswerOption.toString().equalsIgnoreCase(AnswerOption.DISLIKE.toString())) {
									if(disLikedMap.get(questionId) == null){
										disLikedMap.put(questionId, 0L);
									}
									disLikedMap.put(questionId, disLikedMap.get(questionId) + 1);
								}
							}
						}
					}else {
						if (inReviewMap.get(questionId) == null) {
							inReviewMap.put(questionId, 0L);
						}
						inReviewMap.put(questionId, inReviewMap.get(questionId) + 1);
					}
				}
			});
		}

		long totalRecords = questionRepository.count();
		int pages = (int) (totalRecords / 20);
		List<QuestionAnalyticsData> list = new ArrayList<>();
		for (int i = 0; i < pages + 1; i++) {
			Pageable page = PageRequest.of(i, 20);
			Page<Question> questions = questionRepository.findAll(page);
			questions.getContent().stream()
					.filter(question -> correctMap.get(question.getId()) != null
							|| incorrectMap.get(question.getId()) != null || inReviewMap.get(question.getId()) != null || likedMap.get(question.getId()) != null || disLikedMap.get(question.getId()) != null)
					.forEach(question -> {
						QuestionAnalyticsData questionAnalyticsData = new QuestionAnalyticsData();
						String q = question.getQuestion();
						QuestionType type = question.getQuestionType();
						Long correctCount = correctMap.getOrDefault(question.getId(), 0L);
						Long inCorrectCount = incorrectMap.getOrDefault(question.getId(), 0L);
						Long inReviewCount = inReviewMap.getOrDefault(question.getId(), 0L);
						Long unansweredCount = unawnseredMap.getOrDefault(question.getId(), 0L);
						Long likeCount = likedMap.getOrDefault(question.getId(), 0L);
						Long dislikeCount = disLikedMap.getOrDefault(question.getId(), 0L);
						questionAnalyticsData.setCorrectCount(correctCount);
						questionAnalyticsData.setInCorrectCount(inCorrectCount);
						questionAnalyticsData.setInReviewCount(inReviewCount);
						questionAnalyticsData.setUnAnsweredCount(unansweredCount);
						questionAnalyticsData.setQuestionId(question.getId());
						questionAnalyticsData.setLikeCount(likeCount);
						questionAnalyticsData.setDislikeCount(dislikeCount);

						questionAnalyticsData.setQuestionType(type);
						questionAnalyticsData.setQuestion(q);
						questionAnalyticsData.setQuizId(question.getQuizId());
						Answer findByQuestionId = answerRepository.findByQuestionId(question.getId());
						if (findByQuestionId != null) {
							questionAnalyticsData.setAnswer(findByQuestionId.getOption());
						}
						questionAnalyticsData.setAnalyticsTime(timestamp);
						list.add(questionAnalyticsData);
					});
		}
		questionAnalyticsDataRepository.saveAll(list);
	}
}
