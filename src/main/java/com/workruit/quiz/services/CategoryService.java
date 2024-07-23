/**
 * 
 */
package com.workruit.quiz.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.workruit.quiz.controllers.dto.CategoryDTO;
import com.workruit.quiz.controllers.dto.CategoryDetailsDTO;
import com.workruit.quiz.controllers.dto.CategoryListDTO;
import com.workruit.quiz.controllers.dto.CategoryTopicsCountDTO;
import com.workruit.quiz.persistence.entity.Category;
import com.workruit.quiz.persistence.entity.Enterprise;
import com.workruit.quiz.persistence.entity.Enterprise.EnterpriseType;
import com.workruit.quiz.persistence.entity.Quiz;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;
import com.workruit.quiz.persistence.entity.QuizCategoryList;
import com.workruit.quiz.persistence.entity.QuizTopicList;
import com.workruit.quiz.persistence.entity.User;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;
import com.workruit.quiz.persistence.entity.repository.CategoryRepository;
import com.workruit.quiz.persistence.entity.repository.EnterpriseRepository;
import com.workruit.quiz.persistence.entity.repository.QuizCategoryListRepository;
import com.workruit.quiz.persistence.entity.repository.QuizRepository;
import com.workruit.quiz.persistence.entity.repository.QuizTopicListRepository;
import com.workruit.quiz.persistence.entity.repository.UserQuizRepository;
import com.workruit.quiz.persistence.entity.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Santosh
 *
 */
@Service
@Slf4j
public class CategoryService {
	private @Autowired CategoryRepository categoryRepository;
	private @Autowired ModelMapper modelMapper;
	private @Autowired QuizRepository quizRepository;
	private @Autowired QuizCategoryListRepository quizCategoryListRepository;
	private @Autowired QuizTopicListRepository quizTopicListRepository;
	private @Autowired EnterpriseRepository enterpriseRepository;
	private @Autowired UserRepository userRepository;
	private @Autowired UserQuizRepository userQuizRepository;

	public Long save(CategoryDTO categoryDTO) {
		try {
			Category category = modelMapper.map(categoryDTO, Category.class);
			return categoryRepository.save(category).getId();
		} catch (Exception e) {
			log.error("Error while saving category", e);
			throw e;
		}
	}

	public List<CategoryDTO> getByMultiple(List<Long> ids) {
		try {
			List<Category> categories = categoryRepository.findAllById(ids);
			return categories.stream().map(category -> modelMapper.map(category, CategoryDTO.class))
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error while getting categories", e);
			throw e;
		}
	}

	/**
	 * 1. Enterprise Id a. Get Enterprise Details b. Get Quizzes for that
	 * enterprise. c. Get Categories for the quizzes.
	 * 
	 * @param enterpriseId
	 * @param categoryId
	 * @param userId
	 * @return
	 */
	public List<CategoryDetailsDTO> filter(Long enterpriseId, Long categoryId, long userId) {
		try {
			User user = userRepository.findById(userId).get();
			List<CategoryDetailsDTO> categoryDetailsDTOs = new ArrayList<>();

			if (enterpriseId != null) {
				Optional<Enterprise> enterpriseOptional = enterpriseRepository.findById(enterpriseId);
				if (!enterpriseOptional.isPresent()) {
					return categoryDetailsDTOs;
				}
				Enterprise enterprise = enterpriseOptional.get();
				if (enterprise.getEnterpriseType() == EnterpriseType.PRIVATE) {
					if (enterprise.getEnterpriseCode() != null) {
						if (user.getAccessCode() != null) {
							if (!user.getAccessCode().contains(enterprise.getEnterpriseCode())) {
								return categoryDetailsDTOs;
							}
						} else {
							return categoryDetailsDTOs;
						}
					}
				}
				if (categoryId != null) {
					Category category = categoryRepository.findById(categoryId).get();
					if (category.getParentId() == null) {
						List<Quiz> enterpriseQuizs = quizRepository.findByEnterpriseIdAndStatus(enterpriseId,
								QuizSubmitStatus.ACTIVE);
						List<Long> quizIds = enterpriseQuizs.stream().map(enterpriseQuiz -> enterpriseQuiz.getId())
								.collect(Collectors.toList());
						quizIds.removeAll(userQuizRepository.findByUserIdAndQuizIdIn(userId, quizIds).stream()
								.map(obj -> obj)
								.filter(obj -> obj.getStatus() != UserQuizStatus.IN_PROGRESS)
								.map(obj -> obj.getQuizId())
								.collect(Collectors.toList()));
						Set<Long> topics = categoryRepository.findByParentId(categoryId).stream()
								.map(topic -> topic.getId()).collect(Collectors.toSet());
						for (Long topicId : topics) {
							CategoryDetailsDTO categoryDetailsDTO = new CategoryDetailsDTO();
							Category topic = categoryRepository.findById(topicId).get();
							categoryDetailsDTO.setLogoURL(topic.getLogoURL());
							categoryDetailsDTO.setName(topic.getName());
							categoryDetailsDTO.setEnterpriseId(enterpriseId);
							categoryDetailsDTO.setEnterpriseName(enterprise.getName());
							categoryDetailsDTO.setId(topicId);
							Long countByQuizIdInAndTopicId = quizTopicListRepository.countByQuizIdInAndTopicId(quizIds,
									topicId);
							categoryDetailsDTO.setNumberOfQuizs(countByQuizIdInAndTopicId);
							if (countByQuizIdInAndTopicId > 0) {
								categoryDetailsDTOs.add(categoryDetailsDTO);
							}
						}
						return categoryDetailsDTOs;
					} else {
						return null;
					}
				} else {
					List<Quiz> enterpriseQuizs = quizRepository.findByEnterpriseIdAndStatus(enterpriseId,
							QuizSubmitStatus.ACTIVE);
					List<Long> quizIds = enterpriseQuizs.stream().map(enterpriseQuiz -> enterpriseQuiz.getId())
							.collect(Collectors.toList());
					quizIds.removeAll(userQuizRepository.findByUserIdAndQuizIdIn(userId, quizIds).stream()
							.map(obj -> obj)
							.filter(obj -> obj.getStatus() != UserQuizStatus.IN_PROGRESS)
							.map(obj -> obj.getQuizId()).collect(Collectors.toList()));
					List<QuizCategoryList> quizCategoryList = quizCategoryListRepository.findByQuizIdIn(quizIds);
					Map<Long, Long> categoryCounts = quizCategoryList.stream()
							.collect(Collectors.groupingBy(QuizCategoryList::getCategoryId, Collectors.counting()));
					for (Long catId : categoryCounts.keySet()) {
						Category category = categoryRepository.findById(catId).get();
						CategoryDetailsDTO categoryDetailsDTO = new CategoryDetailsDTO();
						categoryDetailsDTO.setLogoURL(category.getLogoURL());
						categoryDetailsDTO.setEnterpriseId(enterprise.getId());
						categoryDetailsDTO.setEnterpriseName(enterprise.getName());
						categoryDetailsDTO.setId(catId);
						categoryDetailsDTO.setName(category.getName());
						List<Long> topics = categoryRepository.findByParentId(catId).stream()
								.map(topic -> topic.getId()).collect(Collectors.toList());
						List<QuizTopicList> quizTopicListizs = quizTopicListRepository
								.findByQuizIdInAndTopicIdIn(quizIds, topics);
						categoryDetailsDTO.setNumberOfTopics(
								quizTopicListizs.stream().map(x -> x.getTopicId()).collect(Collectors.toSet()).size());
						categoryDetailsDTO.setNumberOfQuizs((long) quizTopicListizs.stream().map(x -> x.getQuizId())
								.collect(Collectors.toSet()).size());
						if (categoryDetailsDTO.getNumberOfQuizs() > 0) {
							categoryDetailsDTOs.add(categoryDetailsDTO);
						}
					}
					return categoryDetailsDTOs;
				}
			} else {
				if (categoryId != null) {
					Category category = categoryRepository.findById(categoryId).get();
					if (category.getParentId() == null) {
						List<Quiz> enterpriseQuizs = null;
						if (StringUtils.isNotBlank(user.getAccessCode())) {
							enterpriseQuizs = quizRepository
									.findByStatusByEnterpriseCode(QuizSubmitStatus.ACTIVE.name(), user.getAccessCode());
						} else {
							enterpriseQuizs = quizRepository
									.findByStatusByEnterpriseTypePublic(QuizSubmitStatus.ACTIVE.name());
						}
						List<Long> quizIds = enterpriseQuizs.stream().map(enterpriseQuiz -> enterpriseQuiz.getId())
								.collect(Collectors.toList());
						quizIds.removeAll(userQuizRepository.findByUserIdAndQuizIdIn(userId, quizIds).stream()
								.map(obj -> obj)
								.filter(obj -> obj.getStatus() != UserQuizStatus.IN_PROGRESS)
								.map(obj -> obj.getQuizId()).collect(Collectors.toList()));
						Set<Long> topics = categoryRepository.findByParentId(categoryId).stream()
								.map(topic -> topic.getId()).collect(Collectors.toSet());
						for (Long topicId : topics) {
							CategoryDetailsDTO categoryDetailsDTO = new CategoryDetailsDTO();
							Category topic = categoryRepository.findById(topicId).get();
							categoryDetailsDTO.setLogoURL(topic.getLogoURL());
							categoryDetailsDTO.setName(topic.getName());
							categoryDetailsDTO.setId(topicId);
							Long countByQuizIdInAndTopicId = quizTopicListRepository.countByQuizIdInAndTopicId(quizIds,
									topicId);
							categoryDetailsDTO.setNumberOfQuizs(countByQuizIdInAndTopicId);
							if (countByQuizIdInAndTopicId > 0) {
								categoryDetailsDTOs.add(categoryDetailsDTO);
							}
						}
						return categoryDetailsDTOs;
					} else {

					}
				} else {
					List<Quiz> enterpriseQuizs = null;
					if (StringUtils.isNotBlank(user.getAccessCode())) {
						enterpriseQuizs = quizRepository.findByStatusByEnterpriseCode(QuizSubmitStatus.ACTIVE.name(),
								user.getAccessCode());
					} else {
						enterpriseQuizs = quizRepository
								.findByStatusByEnterpriseTypePublic(QuizSubmitStatus.ACTIVE.name());
					}
					List<Long> quizIds = enterpriseQuizs.stream().map(enterpriseQuiz -> enterpriseQuiz.getId())
							.collect(Collectors.toList());
					quizIds.removeAll(userQuizRepository.findByUserIdAndQuizIdIn(userId, quizIds).stream()
							.map(obj -> obj)
							.filter(obj -> obj.getStatus() != UserQuizStatus.IN_PROGRESS)
							.map(obj -> obj.getQuizId()).collect(Collectors.toList()));
					List<QuizCategoryList> quizCategoryList = quizCategoryListRepository.findByQuizIdIn(quizIds);
					Map<Long, Long> categoryCounts = quizCategoryList.stream()
							.collect(Collectors.groupingBy(QuizCategoryList::getCategoryId, Collectors.counting()));
					for (Long catId : categoryCounts.keySet()) {
						Category category = categoryRepository.findById(catId).get();
						CategoryDetailsDTO categoryDetailsDTO = new CategoryDetailsDTO();
						categoryDetailsDTO.setLogoURL(category.getLogoURL());
						categoryDetailsDTO.setId(catId);
						categoryDetailsDTO.setName(category.getName());
						List<Long> topics = categoryRepository.findByParentId(catId).stream()
								.map(topic -> topic.getId()).collect(Collectors.toList());
						List<QuizTopicList> quizTopicListizs = quizTopicListRepository
								.findByQuizIdInAndTopicIdIn(quizIds, topics);
						categoryDetailsDTO.setNumberOfTopics(
								quizTopicListizs.stream().map(x -> x.getTopicId()).collect(Collectors.toSet()).size());
						Long countByQuizIdInAndTopicIdIn = quizTopicListRepository.countByQuizIdInAndTopicIdIn(quizIds,
								topics);
						categoryDetailsDTO.setNumberOfQuizs(countByQuizIdInAndTopicIdIn);
						if (countByQuizIdInAndTopicIdIn > 0) {
							categoryDetailsDTOs.add(categoryDetailsDTO);
						}
					}
					return categoryDetailsDTOs;
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	public CategoryDTO get(Long categoryId) {
		try {
			Optional<Category> category = categoryRepository.findById(categoryId);
			CategoryDTO categoryDTO = modelMapper.map(category.get(), CategoryDTO.class);
			if (category.get().getParentId() != null) {
				Optional<Category> parentCategory = categoryRepository.findById(category.get().getParentId());
				CategoryDTO parentCategoryDTO = modelMapper.map(parentCategory.get(), CategoryDTO.class);
				categoryDTO.setParentId(parentCategoryDTO.getId());
			}
			return categoryDTO;
		} catch (Exception e) {
			log.error("Error while getting category", e);
			throw e;
		}
	}

	public List<CategoryDTO> list(int pageNumber, int size) {
		PageRequest pageRequest = PageRequest.of(pageNumber, size, Sort.by(Sort.Direction.ASC, "name"));
		List<Category> categories = categoryRepository.findAll(pageRequest).getContent();
		return getCategoryDTOs(categories);
	}

	public long count() {
		return categoryRepository.count();
	}

	public List<CategoryListDTO> categoryDetails() {
		List<Category> categories = categoryRepository.findAll();
		Map<CategoryDTO, List<CategoryDTO>> categoryList = new HashMap<>();
		for (Category category : categories) {
			if (category.getParentId() == null) {
				categoryList.putIfAbsent(new CategoryDTO(category.getId(), category.getName()), new ArrayList<>());
			} else {
				categoryList.get(new CategoryDTO(category.getParentId()))
						.add(new CategoryDTO(category.getId(), category.getName()));
			}
		}
		return categoryList.entrySet().stream().map(entry -> {
			CategoryListDTO categoryListDTO = new CategoryListDTO();
			categoryListDTO.setId(entry.getKey().getId());
			categoryListDTO.setName(entry.getKey().getName());
			categoryListDTO.setTopics(entry.getValue());
			return categoryListDTO;
		}).collect(Collectors.toList());

	}

	public List<CategoryDTO> listCategories() {
		List<Category> categories = categoryRepository.findByParentId(null);
		return getCategoryDTOs(categories);
	}

	private List<CategoryDTO> getCategoryDTOs(List<Category> categories) {
		List<CategoryDTO> categoryDTOs = new ArrayList<CategoryDTO>();
		categories.stream().forEach(category -> {
			CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
			if (category.getParentId() != null) {
				CategoryDTO parentDTO = modelMapper.map(categoryRepository.findById(category.getParentId()).get(),
						CategoryDTO.class);
				categoryDTO.setParentId(parentDTO.getId());
			}
			categoryDTOs.add(categoryDTO);
		});
		return categoryDTOs;
	}

	public List<CategoryTopicsCountDTO> listCategoryCounts() {
		List<Category> categories = categoryRepository.findByParentId(null);
		List<CategoryTopicsCountDTO> catgeCategoryTopicsCountDTOs = new ArrayList<>();
		for (Category category : categories) {
			catgeCategoryTopicsCountDTOs.add(new CategoryTopicsCountDTO(category.getName(),
					categoryRepository.countByParentId(category.getId())));
		}
		return catgeCategoryTopicsCountDTOs;
	}
}
