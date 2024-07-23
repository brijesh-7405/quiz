/**
 * 
 */
package com.workruit.quiz.services;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.controllers.dto.CategoryDTO;
import com.workruit.quiz.controllers.dto.CategoryListDTO;
import com.workruit.quiz.controllers.dto.UserAnalyticsDataDTO;
import com.workruit.quiz.controllers.dto.UserQuizAnalyticsDataDTO;
import com.workruit.quiz.controllers.dto.UserQuizAnalyticsStatsDTO;
import com.workruit.quiz.persistence.entity.Category;
import com.workruit.quiz.persistence.entity.Quiz;
import com.workruit.quiz.persistence.entity.User;
import com.workruit.quiz.persistence.entity.UserCategories;
import com.workruit.quiz.persistence.entity.UserDetails;
import com.workruit.quiz.persistence.entity.UserLocation;
import com.workruit.quiz.persistence.entity.UserQuiz;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;
import com.workruit.quiz.persistence.entity.UserQuizAnalyticsData;
import com.workruit.quiz.persistence.entity.UserQuizStatusContent;
import com.workruit.quiz.persistence.entity.repository.CategoryRepository;
import com.workruit.quiz.persistence.entity.repository.QuestionRepository;
import com.workruit.quiz.persistence.entity.repository.QuizRepository;
import com.workruit.quiz.persistence.entity.repository.UserCategoriesRepository;
import com.workruit.quiz.persistence.entity.repository.UserDetailsRepository;
import com.workruit.quiz.persistence.entity.repository.UserLocationRepository;
import com.workruit.quiz.persistence.entity.repository.UserQuizAnalyticsDataRepository;
import com.workruit.quiz.persistence.entity.repository.UserQuizAnswersRepository;
import com.workruit.quiz.persistence.entity.repository.UserQuizRepository;
import com.workruit.quiz.persistence.entity.repository.UserRepository;
import com.workruit.quiz.persistence.entity.repository.UserTopicsRepository;
import com.workruit.quiz.services.utils.DateUtils;
import com.workruit.quiz.services.utils.PercentageUtils;

/**
 * @author Santosh
 *
 */
@Transactional
@Service
public class UserQuizAnalyticsService {

	private @Autowired UserQuizRepository userQuizRepository;
	private @Autowired UserRepository userRepository;
	private @Autowired QuizRepository quizRepository;
	private @Autowired UserQuizAnalyticsDataRepository userQuizAnalyticsDataRepository;
	private @Autowired ModelMapper modelMapper;
	private @Autowired QuestionRepository questionRepository;
	private @Autowired UserQuizAnswersRepository userQuizAnswersRepository;
	private @Autowired UserLocationRepository userLocationRepository;
	private @Autowired UserDetailsRepository userDetailsRepository;
	private @Autowired UserQuizService userQuizService;
	private @Autowired CategoryRepository categoryRepository;
	private @Autowired UserCategoriesRepository userCategoriesRepository;
	private @Autowired UserTopicsRepository userTopicsRepository;
	ObjectMapper objectMapper = new ObjectMapper();

	public void schedule() {
		persist();
	}

	public List<UserQuizAnalyticsStatsDTO> getStats(Long quizId, Long enterpriseId) throws WorkruitException {

		Quiz quiz = quizRepository.findByIdAndEnterpriseId(quizId, enterpriseId);

		if (quiz == null) {
			throw new WorkruitException("Quiz cannot be accessed by this enterprise");
		}

		List<UserQuizAnalyticsStatsDTO> list = new ArrayList<>();
		Long E = userQuizAnalyticsDataRepository
				.countByStatusAndQuizIdAndPercentageGreaterThanEqualAndPercentageLessThan(UserQuizStatus.COMPLETED,
						quizId, 0, 40);
		list.add(new UserQuizAnalyticsStatsDTO("E", 0, 40, E));
		Long D = userQuizAnalyticsDataRepository
				.countByStatusAndQuizIdAndPercentageGreaterThanEqualAndPercentageLessThan(UserQuizStatus.COMPLETED,
						quizId, 40, 60);
		list.add(new UserQuizAnalyticsStatsDTO("D", 40, 60, D));
		Long C = userQuizAnalyticsDataRepository
				.countByStatusAndQuizIdAndPercentageGreaterThanEqualAndPercentageLessThan(UserQuizStatus.COMPLETED,
						quizId, 60, 75);
		list.add(new UserQuizAnalyticsStatsDTO("C", 60, 75, C));
		Long B = userQuizAnalyticsDataRepository
				.countByStatusAndQuizIdAndPercentageGreaterThanEqualAndPercentageLessThan(UserQuizStatus.COMPLETED,
						quizId, 75, 90);
		list.add(new UserQuizAnalyticsStatsDTO("B", 75, 90, B));
		Long A = userQuizAnalyticsDataRepository
				.countByStatusAndQuizIdAndPercentageGreaterThanEqualAndPercentageLessThan(UserQuizStatus.COMPLETED,
						quizId, 90, 101);
		list.add(new UserQuizAnalyticsStatsDTO("A", 0, 100, A));
		return list;
	}

	public Long count(Long quizId, Long enterpriseId) {
		return userQuizAnalyticsDataRepository.countByQuizIdAndVisibleAndEnterpriseId(quizId, true, enterpriseId);
	}

	public Long countByQuizId(UserQuizStatus status, Long quizId, Long enterpriseId) {
		return userQuizAnalyticsDataRepository.countByStatusAndQuizIdAndEnterpriseId(status, quizId, enterpriseId);
	}

	public Long countByQuizIdAndAutoCompleted(UserQuizStatus status, Long quizId, Long enterpriseId, boolean auto) {
		long count = 0;
		if(auto){
			count = userQuizAnalyticsDataRepository.countByStatusInAndQuizIdAndEnterpriseIdAndAutoCompleted(Arrays.asList(UserQuizStatus.COMPLETED,UserQuizStatus.PENDING_REVIEW), quizId,
					enterpriseId, auto);
		}else {
			count = userQuizAnalyticsDataRepository.countByStatusInAndQuizIdAndEnterpriseIdAndAutoCompleted(Arrays.asList(status), quizId,
					enterpriseId, auto);
		}
		return count;
	}

	public Long countByStatusAndQuizIdAndEnterpriseId(UserQuizStatus status, Long quizId, Long enterpriseId) {
		return userQuizAnalyticsDataRepository.countByStatusAndQuizIdAndEnterpriseId(status, quizId, enterpriseId);
	}

	public Long countByUserId(Long userId) {
		return userQuizAnalyticsDataRepository.countByUserId(userId);
	}

	public Long countByEnterpriseId(Long enterpriseId) {
		return userQuizAnalyticsDataRepository.countByEnterpriseId(enterpriseId);
	}

	public List<UserQuizAnalyticsDataDTO> list(int page, int size, Long quizId, Long enterpriseId) {
		try {
			PageRequest newPAgeRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "analyticsTime"));
			List<UserQuizAnalyticsDataDTO> collect = userQuizAnalyticsDataRepository
					.findByQuizIdAndVisibleAndEnterpriseId(quizId, true, enterpriseId, newPAgeRequest).stream()
					.map(obj -> modelMapper.map(obj, UserQuizAnalyticsDataDTO.class)).collect(Collectors.toList());
			return collect.stream().map(obj -> populatePercentage(obj)).filter(x -> x != null)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw e;
		}
	}

	public List<UserAnalyticsDataDTO> listByUserId(int page, int size, Long userId) {
		try {
			PageRequest newPAgeRequest = PageRequest.of(page, size);
			modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
			List<UserAnalyticsDataDTO> result = userQuizAnalyticsDataRepository.findByUserId(userId, newPAgeRequest)
					.stream().map(obj -> modelMapper.map(obj, UserAnalyticsDataDTO.class)).collect(Collectors.toList());
			result.stream().forEach(obj -> {
				UserLocation userLocation = userLocationRepository.findByUserId(userId);
				if (userLocation != null) {
					obj.setLocation(userLocation.getCity());
				}
				Optional<UserDetails> optional = userDetailsRepository.findById(userId);
				if (optional.isPresent()) {
					obj.setCollege(optional.get().getCollegeName());
				}
				Optional<Quiz> optionalQuiz = quizRepository.findById(obj.getQuizId());
				if (optionalQuiz.isPresent()) {
					obj.setQuizName(optionalQuiz.get().getName());
					obj.setLevel(optionalQuiz.get().getLevel().name());
					obj.setExpiryDate(DateUtils.format(optionalQuiz.get().getExpiryDate()));
				}
				obj.setCategory(userQuizService.getCategoryNamesForQuiz(obj.getQuizId()));
				obj.setTopic(userQuizService.getTopicNamesForQuiz(obj.getQuizId()));
				List<UserCategories> categories = userCategoriesRepository.findByUserId(userId);
				Map<CategoryDTO, List<CategoryDTO>> categoryMap = new HashMap<>();
				categories.forEach(userCategory -> {
					Category category = categoryRepository.findById(userCategory.getCategoryId()).get();
					CategoryDTO categoryDTO = new CategoryDTO(category.getId(), category.getName());
					categoryMap.putIfAbsent(categoryDTO, new ArrayList<>());
					List<Long> userTopicIds = userTopicsRepository.findByUserId(userId).stream()
							.map(x -> x.getTopicId()).collect(Collectors.toList());
					List<Category> findByParentId = categoryRepository.findByParentId(category.getId());
					List<Long> topicIds = findByParentId.stream().map(x -> x.getId()).collect(Collectors.toList());
					userTopicIds.retainAll(topicIds);
					categoryMap.put(categoryDTO, findByParentId.stream().filter(x -> userTopicIds.contains(x.getId()))
							.map(y -> new CategoryDTO(y.getId(), y.getName())).collect(Collectors.toList()));

					obj.setUserInterests(categoryMap.entrySet().stream().map(entry -> {
						CategoryListDTO categoryListDTO = new CategoryListDTO();
						categoryListDTO.setId(entry.getKey().getId());
						categoryListDTO.setName(entry.getKey().getName());
						categoryListDTO.setTopics(entry.getValue());
						return categoryListDTO;
					}).collect(Collectors.toList()));
				});

				if (obj.getStatus() == UserQuizStatus.COMPLETED) {
					populatePercentage(obj);
				} else {
					populateProgressPercentage(obj);
				}
			});
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

	public List<UserQuizAnalyticsDataDTO> listByEnterpriseId(int page, int size, Long enterpriseId) {
		try {
			PageRequest newPAgeRequest = PageRequest.of(page, size);
			return userQuizAnalyticsDataRepository.findByEnterpriseId(enterpriseId, newPAgeRequest).stream()
					.map(obj -> modelMapper.map(obj, UserQuizAnalyticsDataDTO.class)).collect(Collectors.toList());
		} catch (Exception e) {
			throw e;
		}
	}

	public List<UserQuizAnalyticsDataDTO> listByStatus(UserQuizStatus status, int page, int size, Long quizId,
			Long enterpriseId, boolean autoCompleted) {
		try {
			PageRequest newPAgeRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "analyticsTime"));
			List<UserQuizAnalyticsDataDTO> listOfAnalyticsData = new ArrayList<>();
			if (autoCompleted) {
				listOfAnalyticsData = userQuizAnalyticsDataRepository
						.findByStatusInAndQuizIdAndEnterpriseIdAndAutoCompleted(Arrays.asList(UserQuizStatus.COMPLETED,UserQuizStatus.PENDING_REVIEW), quizId, enterpriseId,
								autoCompleted, newPAgeRequest)
						.stream().map(obj -> modelMapper.map(obj, UserQuizAnalyticsDataDTO.class))
						.collect(Collectors.toList());
				listOfAnalyticsData.stream().forEach(obj -> {
					populatePercentage(obj);
				});
				listOfAnalyticsData.stream().forEach(obj -> {
					obj.setUserQuizStartedDate(DateUtils.format(obj.getQuizStartedDate()));
					obj.setQuizPostedDate(DateUtils.format(obj.getPostedDate()));
				});
			} else {
				if(status == UserQuizStatus.COMPLETED){
					listOfAnalyticsData = userQuizAnalyticsDataRepository
							.findByStatusInAndQuizIdAndEnterpriseIdAndAutoCompleted(Arrays.asList(UserQuizStatus.COMPLETED), quizId, enterpriseId,
									autoCompleted, newPAgeRequest)
							.stream().map(obj -> modelMapper.map(obj, UserQuizAnalyticsDataDTO.class))
							.collect(Collectors.toList());
					listOfAnalyticsData.stream().forEach(obj -> {
						populatePercentage(obj);
					});
					listOfAnalyticsData.stream().forEach(obj -> {
						obj.setUserQuizStartedDate(DateUtils.format(obj.getQuizStartedDate()));
						obj.setQuizPostedDate(DateUtils.format(obj.getPostedDate()));
					});
				}
				else {
					listOfAnalyticsData = userQuizAnalyticsDataRepository
							.findByStatusInAndQuizIdAndEnterpriseIdAndAutoCompleted(Arrays.asList(status), quizId, enterpriseId,
									autoCompleted, newPAgeRequest)
							.stream().map(obj -> modelMapper.map(obj, UserQuizAnalyticsDataDTO.class))
							.collect(Collectors.toList());
					listOfAnalyticsData.stream().forEach(obj -> {
						obj.setUserQuizStartedDate(DateUtils.format(obj.getQuizStartedDate()));
						obj.setQuizPostedDate(DateUtils.format(obj.getPostedDate()));
					});
				}
			}
			return listOfAnalyticsData;
		} catch (Exception e) {
			throw e;
		}

	}

	public List<UserQuizAnalyticsDataDTO> listByStatusWithoutAutoComplete(UserQuizStatus status, int page, int size,
			Long quizId, Long enterpriseId) {
		try {
			PageRequest newPAgeRequest = PageRequest.of(page, size, Sort.by(Direction.DESC, "analyticsTime"));

			if (status == UserQuizStatus.COMPLETED) {
				List<UserQuizAnalyticsDataDTO> listOfAnalyticsData = userQuizAnalyticsDataRepository
						.findByStatusAndQuizIdAndEnterpriseId(status, quizId, enterpriseId, newPAgeRequest).stream()
						.map(obj -> modelMapper.map(obj, UserQuizAnalyticsDataDTO.class)).collect(Collectors.toList());
				listOfAnalyticsData.stream().forEach(obj -> {
					populatePercentage(obj);
				});
				listOfAnalyticsData.stream().forEach(obj -> {
					obj.setUserQuizStartedDate(DateUtils.format(obj.getQuizStartedDate()));
					obj.setQuizPostedDate(DateUtils.format(obj.getPostedDate()));
				});
				return listOfAnalyticsData;
			} else {
				List<UserQuizAnalyticsDataDTO> listOfAnalyticsData = userQuizAnalyticsDataRepository
						.findByStatusAndQuizIdAndEnterpriseId(status, quizId, enterpriseId, newPAgeRequest).stream()
						.map(obj -> modelMapper.map(obj, UserQuizAnalyticsDataDTO.class)).collect(Collectors.toList());
				listOfAnalyticsData.stream().forEach(obj -> {
					obj.setUserQuizStartedDate(DateUtils.format(obj.getQuizStartedDate()));
					obj.setQuizPostedDate(DateUtils.format(obj.getPostedDate()));
				});
				return listOfAnalyticsData;
			}
		} catch (Exception e) {
			throw e;
		}

	}

	private UserAnalyticsDataDTO populateProgressPercentage(UserAnalyticsDataDTO obj) {
		ObjectMapper objectMapper = new ObjectMapper();
		Optional<UserQuiz> optional = userQuizRepository.findById(obj.getUserQuizId());
		if (optional.isPresent()) {
			obj.setStartedDate(DateUtils.format(optional.get().getCreatedDate()));
			try {
				obj.setStatus(optional.get().getStatus());
				if (optional.get().getResultContent() != null) {
					UserQuizStatusContent userQuizStatusContent = objectMapper
							.readValue(optional.get().getResultContent(), UserQuizStatusContent.class);
					long total = userQuizStatusContent.getAnswersStatus().size() - userQuizStatusContent.getLikeDislikeCount();
					long actualTotal = userQuizStatusContent.getAnswersStatus().values().stream().filter(x -> x != null)
							.count();
					obj.setProgressPercentage(
							Math.round(PercentageUtils.getCorrectPercentageInInt(actualTotal, total)));
					obj.setActualProgressPercentage(PercentageUtils.getPercentageInDouble(actualTotal, total));
					return obj;
				} else {
					return obj;
				}
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}

	private UserAnalyticsDataDTO populatePercentage(UserAnalyticsDataDTO obj) {
		ObjectMapper objectMapper = new ObjectMapper();
		Optional<UserQuiz> optional = userQuizRepository.findById(obj.getUserQuizId());
		if (optional.isPresent()) {
			try {
				obj.setStatus(optional.get().getStatus());
				obj.setStartedDate(DateUtils.format(optional.get().getCreatedDate()));
				if (optional.get().getResultContent() != null) {
					UserQuizStatusContent userQuizStatusContent = objectMapper
							.readValue(optional.get().getResultContent(), UserQuizStatusContent.class);
					long totalCount = userQuizStatusContent.getNumberOfQuestions();
					long correctCount = userQuizStatusContent.getCorrectAnswersCount();
					obj.setPercentage(Math.round(PercentageUtils.getPercentageInDouble(correctCount, totalCount)));
					obj.setActualPercentage(PercentageUtils.getPercentageInDouble(correctCount, totalCount));
					return obj;
				} else {
					return obj;
				}
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}

	private UserQuizAnalyticsDataDTO populatePercentage(UserQuizAnalyticsDataDTO obj) {
		ObjectMapper objectMapper = new ObjectMapper();
		Optional<UserQuiz> optional = userQuizRepository.findById(obj.getUserQuizId());
		if (optional.isPresent()) {
			try {
				obj.setQuizPostedDate(DateUtils.format(obj.getPostedDate()));
				obj.setUserQuizStartedDate(DateUtils.format(obj.getQuizStartedDate()));
				if (optional.get().getResultContent() != null) {
					UserQuizStatusContent userQuizStatusContent = objectMapper
							.readValue(optional.get().getResultContent(), UserQuizStatusContent.class);
					long totalCount = userQuizStatusContent.getNumberOfQuestions() - userQuizStatusContent.getLikeDislikeCount();
					long correctCount = userQuizStatusContent.getCorrectAnswersCount();
					obj.setPercentage(Math.round(PercentageUtils.getPercentageInDouble(correctCount, totalCount)));
					obj.setActualPercentage(PercentageUtils.getPercentageInDouble(correctCount, totalCount));
					return obj;
				} else {
					return obj;
				}
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}

	public void persist() {
		try {
			List<UserQuizAnalyticsData> userQuizAnalyticsDatas = new ArrayList<>();
			userQuizRepository.findAll().stream().forEach(userQuiz -> {
				UserQuizAnalyticsData userQuizAnalyticsData = processUserQuiz(userQuiz);
				userQuizAnalyticsDatas.add(userQuizAnalyticsData);
			});
			userQuizAnalyticsDataRepository.saveAll(userQuizAnalyticsDatas);
		} catch (Exception e) {

		}
	}

	public void persistOne(Long userQuizId) {
		UserQuiz userQuiz = userQuizRepository.findById(userQuizId).get();
		userQuizAnalyticsDataRepository.deleteByUserQuizId(userQuizId);
		userQuizAnalyticsDataRepository.save(processUserQuiz(userQuiz));
	}

	private UserQuizAnalyticsData processUserQuiz(UserQuiz userQuiz) {
		Timestamp analyticsTime = new Timestamp(new Date().getTime());
		Quiz quiz = quizRepository.findById(userQuiz.getQuizId()).get();
		User user = userRepository.findById(userQuiz.getUserId()).get();
		UserQuizAnalyticsData userQuizAnalyticsData = new UserQuizAnalyticsData(userQuiz.getQuizId(),
				quiz.getEnterpriseId(), userQuiz.getId(), userQuiz.getStatus(), quiz.getCreatedDate(),
				userQuiz.getCreatedDate());
		userQuizAnalyticsData.setFirstName(user.getFirstName());
		userQuizAnalyticsData.setLastName(user.getLastName());
		userQuizAnalyticsData.setAnalyticsTime(analyticsTime);
		userQuizAnalyticsData.setMobile(user.getMobile());
		userQuizAnalyticsData.setEmail(user.getPrimaryEmail());
		userQuizAnalyticsData.setUserId(user.getId());
		userQuizAnalyticsData.setVisible(userQuiz.isVisible());
		if (userQuiz.getStatus() == UserQuizStatus.COMPLETED || userQuiz.getStatus() == UserQuizStatus.PENDING_REVIEW) {
			userQuizAnalyticsData.setAutoCompleted(userQuiz.isQuizTimedOut());
		}
		if (userQuiz.getStatus() == UserQuizStatus.COMPLETED) {
			try {
				UserQuizStatusContent userQuizStatusContent = objectMapper.readValue(userQuiz.getResultContent(),
						UserQuizStatusContent.class);
				userQuizAnalyticsData.setPercentage((int) (userQuizStatusContent.getCorrectAnswersCount() * 100
						/ (userQuizStatusContent.getNumberOfQuestions() - userQuizStatusContent.getLikeDislikeCount())));
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

		}
		return userQuizAnalyticsData;
	}

}
