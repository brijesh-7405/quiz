/**
 * 
 */
package com.workruit.quiz.services;

import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.workruit.quiz.controllers.dto.*;
import com.workruit.quiz.persistence.entity.*;
import com.workruit.quiz.persistence.entity.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.ExceptionResponse.FieldError;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;
import com.workruit.quiz.services.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Santosh
 *
 */
@Slf4j
@Service
public class QuestionAnswerService {
	private @Autowired QuestionRepository questionRepository;
	private @Autowired AnswerRepository answerRepository;
	private @Autowired CategoryRepository categoryRepository;
	private @Autowired QuizRepository quizRepository;
	private ObjectMapper objectMapper = new ObjectMapper();
	private @Autowired AnalyticsService analyticsService;
	private @Autowired AWSService awsService;
	private @Autowired SubscriptionLimitService subscriptionLimitService;
	private @Autowired QuestionImportAsyncStatusRepository questionImportAsyncStatusRepository;
	private @Autowired QuizCategoryListRepository quizCategoryListRepository;
	private @Autowired QuizTopicListRepository quizTopicListRepository;
	@Value("${question.images.bucket}")
	private String questionImagesBucket;
	@Value("${answers.audio.bucket}")
	private String answersAudioBucket;

	private static final Logger logger = LoggerFactory.getLogger(QuestionAnswerService.class);

	@Transactional
	public void update(QuestionAnswerDTO questionAnswerDTO, Long questionId) throws Exception {
		questionAnswerDTO.getQuestion().setQuestionId(questionId);
		List<QuizSubmitStatus> multipleStatus = new ArrayList<>();
		multipleStatus.add(QuizSubmitStatus.PENDING);
		multipleStatus.add(QuizSubmitStatus.REJECT);
		Quiz quiz = quizRepository.findByIdAndStatusIn(questionAnswerDTO.getQuizId(), multipleStatus);
		if (quiz == null) {
			throw new WorkruitException("Quiz does not exist or already active");
		}
		if (DateUtils.nextDayStartingTime(quiz.getExpiryDate()).getTime()
				- DateUtils.resetTime(new Date()).getTime() <= 0L) {
			throw new WorkruitException("Expiry Date cannot be past than current date");
		}

		save(questionAnswerDTO, true);
		analyticsService.updateQuestionActions(questionId);
	}

	public Long countQuestions(Long quizId) {
		return questionRepository.countByQuizId(quizId);
	}

	public List<QuestionAnalyticsDataDTO> questions(Long quizId, int page, int size) {
		try {
			final ObjectMapper objectMapper = new ObjectMapper();
			List<Question> questions = questionRepository.findByQuizId(quizId, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC,"id")));
			List<QuestionAnalyticsDataDTO> questionAnalyticsDataDTOs = new ArrayList<>();
			questions.stream().forEach(obj -> {
				QuestionAnalyticsDataDTO questionAnalyticsDataDTO = new QuestionAnalyticsDataDTO();
				try {
					questionAnalyticsDataDTO.setId(obj.getId());
					questionAnalyticsDataDTO.setQuestionId(obj.getId());
					questionAnalyticsDataDTO.setQuestionType(obj.getQuestionType());
					JsonNode questionObj = objectMapper.readValue(obj.getQuestion().toString(), JsonNode.class);
					((ObjectNode) questionObj).put("options", objectMapper.readValue(obj.getOptions(), JsonNode.class));
					if(questionObj.get("explanationImage") != null && !questionObj.get("explanationImage").asText().equals("null")) {
						String  key = questionObj.get("explanationImage").asText();
						((ObjectNode) questionObj).put("explanationImage", objectMapper.convertValue(getImage(key), String.class));
						((ObjectNode) questionObj).put("explanationImageKey", objectMapper.convertValue(key, String.class));
					}
					questionAnalyticsDataDTO.setQuestion(questionObj);
					questionAnalyticsDataDTO.setAnswer(objectMapper
							.readValue(answerRepository.findByQuestionId(obj.getId()).getOption(), JsonNode.class));
					questionAnalyticsDataDTO.setQuizId(quizId);
					questionAnalyticsDataDTOs.add(questionAnalyticsDataDTO);
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			return questionAnalyticsDataDTOs;
		} catch (Exception e) {
			throw e;
		}
	}

	@Transactional
	public QuestionCreateResponseDTO save(QuestionAnswerDTO questionAnswerDTO, boolean isUpdate) throws Exception {
		try {
			QuestionDTO questionDTO = questionAnswerDTO.getQuestion();
			Question question = new Question();
			Optional<Quiz> optionalQuiz = quizRepository.findById(questionAnswerDTO.getQuizId());
			if (!optionalQuiz.isPresent()) {
				throw new WorkruitException("Quiz is not present");
			}
			if (!isUpdate) {
				subscriptionLimitService.checkQuestionsLimit(optionalQuiz.get().getEnterpriseId(),
						optionalQuiz.get().getId());
			}
			if (DateUtils.nextDayStartingTime(optionalQuiz.get().getExpiryDate()).getTime()
					- DateUtils.resetTime(new Date()).getTime() <= 0L) {
				throw new WorkruitException("Expiry Date cannot be past than current date");
			}
			question.setQuizId(questionAnswerDTO.getQuizId());
			question.setQuestion(objectMapper.writeValueAsString(questionDTO.getQuestionObj()));
			question.setQuestionType(questionDTO.getQuestionType());
			if (questionDTO.getOptions() != null) {
				for (QuestionOptionDTO questionOptionDTO : questionDTO.getOptions()) {
					if (questionOptionDTO != null && (questionOptionDTO.getType().equals("TEXT")
							|| questionOptionDTO.getType().equals("IMAGE")
							|| questionOptionDTO.getType().equals("VIDEO"))) {

					} else {
						throw new WorkruitException("Valid types for type are TEXT/IMAGE");
					}
				}
			} else {
				throw new WorkruitException("Input question cannot be empty");
			}
			question.setOptions(objectMapper.writeValueAsString(questionDTO.getOptions()));
			if (questionDTO.getQuestionId() != null) {
				question.setId(questionDTO.getQuestionId());

			}
			question = questionRepository.save(question);
			if (questionAnswerDTO.getAnswer().getOptions() != null) {
				for (AnswerOptionDTO answerOption : questionAnswerDTO.getAnswer().getOptions()) {
					if (answerOption.getType() != null
							&& (answerOption.getType().equals("TEXT") || answerOption.getType().equals("IMAGE"))) {

					} else {
						throw new WorkruitException("Valid types for type are TEXT/IMAGE");
					}
				}
			}
			QuestionCreateResponseDTO questionCreateResponseDTO = new QuestionCreateResponseDTO();
			questionCreateResponseDTO.setId(question.getId());
			questionCreateResponseDTO.setQuestion(objectMapper
					.convertValue(questionAnswerDTO.getQuestion().getQuestionObj().getQuestion(), JsonNode.class));
			String answerValue = objectMapper.writeValueAsString(questionAnswerDTO.getAnswer());
			Answer answer = answerRepository.findByQuestionId(question.getId());
			if (answer != null && StringUtils.isBlank(answerValue)) {
				answerRepository.delete(answer);
				analyticsService.updateQuestionActions(question.getId());
				return questionCreateResponseDTO;
			}
			if (answer == null) {
				answer = new Answer();
			}
			answer.setOption(answerValue);
			answer.setQuestionId(question.getId());
			if (StringUtils.isNotBlank(answerValue)) {
				analyticsService.updateQuestionActions(question.getId());
				answer = answerRepository.save(answer);
			} else {
				return questionCreateResponseDTO;
			}
			return questionCreateResponseDTO;
		} catch (JsonProcessingException e) {
			log.error("Error while parsing the question/answer", e);
			throw e;
		} catch (Exception e) {
			log.error("Error while saving question/answer", e);
			throw e;
		}
	}

	@Transactional
	public List<QuestionCreateResponseDTO> saveAll(QuestionAnswerMultipleDTO questionAnswerMultipleDTO)
			throws Exception {
		List<QuizSubmitStatus> multipleStatus = new ArrayList<>();
		multipleStatus.add(QuizSubmitStatus.PENDING);
		multipleStatus.add(QuizSubmitStatus.REJECT);
		Quiz quiz = quizRepository.findByIdAndStatusIn(questionAnswerMultipleDTO.getQuizId(), multipleStatus);
		if (quiz == null) {
			throw new WorkruitException("Quiz does not exist or already active");
		}
		if (DateUtils.nextDayStartingTime(quiz.getExpiryDate()).getTime()
				- DateUtils.resetTime(new Date()).getTime() <= 0L) {
			throw new WorkruitException("Expiry Date cannot be past than current date");
		}
		List<QuestionCreateResponseDTO> questionResponses = new ArrayList<>();
		for (QuestionAnswerDTO questionAnswerDTO : questionAnswerMultipleDTO.getQuestionAnswers()) {
			questionAnswerDTO.setQuizId(questionAnswerMultipleDTO.getQuizId());
			questionResponses.add(save(questionAnswerDTO, false));
		}
		return questionResponses;
	}

	public String saveImage(MultipartFile multipartFile, Long quizId) throws IOException {
		String bucketName = questionImagesBucket;
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(multipartFile.getContentType());
		String key = quizId + "-" + RandomUtils.nextLong(0, Long.MAX_VALUE);
		PutObjectRequest request = new PutObjectRequest(bucketName, key, multipartFile.getInputStream(), metadata);
		PutObjectResult putObjectResult = awsService.getS3Client().putObject(request);
		System.out.println(putObjectResult.getETag());
		return key;
	}

	public String saveAudio(MultipartFile multipartFile, Long userQuizId, Long questionId) throws IOException {
		String bucketName = answersAudioBucket;
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(multipartFile.getContentType());
		String key = userQuizId + "-" + questionId + "-" + RandomUtils.nextLong(0, Long.MAX_VALUE);
		PutObjectRequest request = new PutObjectRequest(bucketName, key, multipartFile.getInputStream(), metadata);
		PutObjectResult putObjectResult = awsService.getS3Client().putObject(request);
		System.out.println(putObjectResult.getETag());
		return key;
	}

	public String getImage(String key) throws IOException {
		String bucketName = questionImagesBucket;
		URL url = awsService.getS3Client().generatePresignedUrl(bucketName, key, DateUtils.next10Min());
		return Base64.getEncoder().encodeToString(IOUtils.toByteArray(url));
	}

	public String getAudio(String key) {
		String bucketName = answersAudioBucket;
		URL url = awsService.getS3Client().generatePresignedUrl(bucketName, key, DateUtils.next10Min());
		return url.toString();
	}

	public Long createUploadJob(MultipartFile multipartFile, long userId) throws IOException {
		InputStream inputStream = multipartFile.getInputStream();
		long time = System.currentTimeMillis();
		final File file = new File(time + ".csv");
		OutputStream outStream = new FileOutputStream(file);
		byte[] buffer = new byte[8 * 1024];
		int bytesRead;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}
		outStream.close();

		QuestionImportAsyncStatus questionImportAsyncStatus = new QuestionImportAsyncStatus();
		questionImportAsyncStatus.setStatus(QuestionImportAsyncStatus.QuestionImportJobStatus.CREATED);
		questionImportAsyncStatus.setUserId(userId);
		if (questionImportAsyncStatusRepository.findVersionByQuestionImportAsyncUserId(userId) != null) {
			questionImportAsyncStatus.setVersion(questionImportAsyncStatusRepository.findVersionByQuestionImportAsyncUserId(userId) + 1);
		} else {
			questionImportAsyncStatus.setVersion(1l);
		}
		questionImportAsyncStatus = questionImportAsyncStatusRepository.save(questionImportAsyncStatus);
		Long id = questionImportAsyncStatus.getQuestionImportAsyncStatusId();
		Thread t = new Thread(() -> {
			try {
				processFile(file, id);
			} catch (Exception e) {
				QuestionImportAsyncStatus questionImportAsyncStatusNew = questionImportAsyncStatusRepository.findById(id).get();
				questionImportAsyncStatusNew.setStatus(QuestionImportAsyncStatus.QuestionImportJobStatus.FAILED);
				questionImportAsyncStatusNew.setDescription("Failed due to " + e.getMessage());
				questionImportAsyncStatusRepository.save(questionImportAsyncStatusNew);
				e.printStackTrace();
			}
		});
		t.start();
		return questionImportAsyncStatus.getQuestionImportAsyncStatusId();
	}


	@Transactional
	public List<CSVRecord> processFile(File file, Long questionImportId)
			throws Exception {
		CSVParser csvParser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT.withHeader());
		List<CSVRecord> records = csvParser.getRecords();
		int index = 0;
		Map<String,Long> quizIds = new HashMap<>();
		List<String> categoryNames = new ArrayList<>();
		List<String> topicsNames = new ArrayList<>();
		Long categoryId = null;
		for (CSVRecord record : records) {
			if(record.get("quizId")!=null && !record.get("quizId").isEmpty() && quizIds.get(record.get("quizId")) == null) {
//				boolean limitCrossed = subscriptionLimitService.checkQuizLimit(Long.parseLong(record.get("enterpriseId")));
//				if (!limitCrossed) {
//					logger.error("Maximum Quiz Limit reached: "+record.get("enterpriseId"));
//					continue;
//				}
				Quiz newQuiz = new Quiz();
				newQuiz.setEnterpriseId(Long.parseLong(record.get("enterpriseId")));
				if (record.get("Quiz Name") != null && !record.get("Quiz Name").isEmpty()) {
					newQuiz.setName(record.get("Quiz Name"));
				}
				else {
					logger.error("Quiz Name cannot be blank");
					continue;
				}
				if (record.get("Code") != null && !record.get("Code").isEmpty()) {
					newQuiz.setCode(record.get("Code"));
				}
				if (record.get("Level") != null && !record.get("Level").isEmpty()) {
					newQuiz.setLevel(Quiz.QuizLevel.valueOf(record.get("Level")));
				}
				if (record.get("Target Audience") != null && !record.get("Target Audience").isEmpty()) {
					newQuiz.setTargetAudience(record.get("Target Audience"));
				}
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				if (record.get("QuizTimeLimit") != null && !record.get("QuizTimeLimit").isEmpty() && record.get("QuizTimeLimit").split(":").length == 3) {
					newQuiz.setQuizTimeLimit(record.get("QuizTimeLimit"));
				}
				else {
					logger.error("QuizTimeLimit cannot be blank");
					continue;
				}
				if(record.get("ExpiryDate") != null && !record.get("ExpiryDate").isEmpty()) {
					Date expiryDate = new Date(dateFormat.parse(record.get("ExpiryDate")).getTime());
					if (DateUtils.nextDayStartingTime(expiryDate).getTime() - DateUtils.resetTime(new Date()).getTime() <= 0L) {
						logger.error("Expiry Date cannot be past than current date");
						continue;
					}
					newQuiz.setExpiryDate(expiryDate);
					//subscriptionLimitService.checkQuizExpiry(newQuiz.getEnterpriseId(), new Timestamp(expiryDate.getTime()));
				}
				else {
					logger.error("Expiry Date cannot be blank");
					continue;
				}

				newQuiz.setStatus(QuizSubmitStatus.ACTIVE);
				newQuiz.setActivatedDate(new Timestamp(new Date().getTime()));
				newQuiz.setUuid(RandomStringUtils.randomAlphanumeric(6));

				Quiz persistentQuiz = quizRepository.save(newQuiz);
				categoryNames.clear();
				topicsNames.clear();
				quizIds.put(record.get("quizId").toString(), persistentQuiz.getId());
			}

			if(quizIds.get(record.get("quizId")) != null) {
				Long newQuizId = quizIds.get(record.get("quizId"));
				if(record.get("Category")!=null && !record.get("Category").isEmpty() && !categoryNames.contains(record.get("Category").toLowerCase())) {
					String categoryName = record.get("Category").toString();
					Category existingCategory = categoryRepository.findByName(categoryName);
					if (existingCategory == null ) {
						Category category = new Category();
						category.setName(categoryName);
						category.setDescription(categoryName);
						Category createdCategory = categoryRepository.save(category);
						categoryId = createdCategory.getId();
						categoryNames.add(createdCategory.getName().toLowerCase());
					} else {
						categoryId = existingCategory.getId();
						categoryNames.add(existingCategory.getName().toLowerCase());
					}
					quizCategoryListRepository.save(new QuizCategoryList(categoryId,newQuizId));
				}
				if(record.get("Topic Name")!=null && !record.get("Topic Name").isEmpty() && !topicsNames.contains(record.get("Topic Name").toLowerCase())){
					String topicName = record.get("Topic Name").toString();
					long topicId;
					Category existingTopic = categoryRepository.findByName(topicName);
					if (existingTopic == null) {
						Category topic = new Category();
						topic.setName(topicName);
						topic.setDescription(topicName);
						if(categoryId != null) {
							topic.setParentId(categoryId);
						}
						Category createdCategory = categoryRepository.save(topic);
						topicId = createdCategory.getId();
						topicsNames.add(createdCategory.getName().toLowerCase());
					} else {
						topicId = existingTopic.getId();
						topicsNames.add(existingTopic.getName().toLowerCase());
					}
					quizTopicListRepository.save(new QuizTopicList(topicId,newQuizId));
				}

				Question question = new Question();
				if (record.get("question_type") != null && !record.get("question_type").isEmpty()) {
					question.setQuestionType(Question.QuestionType.valueOf(record.get("question_type")));
				}
				question.setQuizId(newQuizId);
				if(record.get("question") != null){
					QuestionObjectDTO questionObjectDTO = objectMapper.readValue(record.get("question"), new TypeReference<QuestionObjectDTO>() {});
					if(questionObjectDTO.getExplanationImage() != null && !questionObjectDTO.getExplanationImage().equals("") && isBase64Encoded(questionObjectDTO.getExplanationImage())) {
						byte[] decodedBytes = Base64.getDecoder().decode(questionObjectDTO.getExplanationImage());
 						if (decodedBytes != null && decodedBytes.length != 0) {
							BASE64DecodedMultipartFile base64DecodedMultipartFile = new BASE64DecodedMultipartFile(decodedBytes);
							String key = saveImage(base64DecodedMultipartFile, newQuizId);
							questionObjectDTO.setExplanationImage(key);
						}
					}
					else{
						questionObjectDTO.setExplanationImage(null);
					}
					String question1 = objectMapper.writeValueAsString(questionObjectDTO);
					question.setQuestion(question1);
				}
				if (record.get("options") != null) {
					List<QuestionOptionDTO> questionOptionDTOS = objectMapper.readValue(record.get("options"), new TypeReference<List<QuestionOptionDTO>>() {
					});
					for (QuestionOptionDTO questionOptionDTO : questionOptionDTOS) {
						if (questionOptionDTO != null && (questionOptionDTO.getType().equals("TEXT")
								|| questionOptionDTO.getType().equals("IMAGE")
								|| questionOptionDTO.getType().equals("VIDEO"))) {

						} else {
							throw new WorkruitException("Valid types for type are TEXT/IMAGE");
						}
					}
					question.setOptions(record.get("options"));
				} else {
					throw new WorkruitException("Input question cannot be empty");
				}


				Question savedQuestion = questionRepository.save(question);

				Answer answer = new Answer();
				answer.setQuestionId(savedQuestion.getId());
				if (record.get("answer") != null) {
					List<AnswerOptionDTO> answerOptionDTOS = objectMapper.readValue(record.get("options"), new TypeReference<List<AnswerOptionDTO>>() {
					});
					for (AnswerOptionDTO answerOption : answerOptionDTOS) {
						if (answerOption.getType() != null
								&& (answerOption.getType().equals("TEXT") || answerOption.getType().equals("IMAGE"))) {

						} else {
							throw new WorkruitException("Valid types for type are TEXT/IMAGE");
						}
					}
					answer.setOption(record.get("answer"));
				}
				answerRepository.save(answer);
				analyticsService.updateQuestionActions(savedQuestion.getId());
				index++;
				if (index % 10 == 0) {
					saveQuestionImportStatus(index, records.size(), questionImportId);
				}
			}
		}
		saveQuestionImportStatus(records.size(), records.size(), questionImportId);
		return records;
	}
	public static boolean isBase64Encoded(String input) {
		try {
			Base64.getDecoder().decode(input);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveQuestionImportStatus(int index, int totalSize, Long questionImportId){
		QuestionImportAsyncStatus questionImportAsyncStatus = questionImportAsyncStatusRepository.findById(questionImportId).get();
		int currentStatus = index * 100 / totalSize;
		if (currentStatus < 100) {
			questionImportAsyncStatus.setStatus(QuestionImportAsyncStatus.QuestionImportJobStatus.INPROGRESS);
		} else {
			questionImportAsyncStatus.setStatus(QuestionImportAsyncStatus.QuestionImportJobStatus.SUCCESS);
		}
		questionImportAsyncStatus.setDescription(currentStatus + "% records have been processed");
		questionImportAsyncStatusRepository.save(questionImportAsyncStatus);
	}
	public QuestionImportJobStatusDTO getJobStatus(Long jobId, Long userId) throws Exception {
		QuestionImportAsyncStatus questionImportAsyncStatus = questionImportAsyncStatusRepository
				.findByQuestionImportAsyncStatusIdAndUserId(jobId, userId);
		if (questionImportAsyncStatus != null) {
			QuestionImportJobStatusDTO questionImportJobStatusDTO = new QuestionImportJobStatusDTO();
			questionImportJobStatusDTO.setDescription(questionImportAsyncStatus.getDescription());
			questionImportJobStatusDTO.setStatus(questionImportAsyncStatus.getStatus());
			return questionImportJobStatusDTO;
		} else {
			throw new Exception("No matching job found");
		}
	}
}
