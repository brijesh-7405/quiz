/**
 *
 */
package com.workruit.quiz.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.workruit.quiz.configuration.WorkruitException;
import com.workruit.quiz.constants.AnswerOption;
import com.workruit.quiz.constants.QuizVisibility;
import com.workruit.quiz.controllers.dto.*;
import com.workruit.quiz.controllers.dto.UserQuestionAnalyticsDataDTO.AnswerStatus;
import com.workruit.quiz.persistence.entity.*;
import com.workruit.quiz.persistence.entity.Question.QuestionType;
import com.workruit.quiz.persistence.entity.Quiz.QuizSubmitStatus;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizResult;
import com.workruit.quiz.persistence.entity.UserQuiz.UserQuizStatus;
import com.workruit.quiz.persistence.entity.repository.*;
import com.workruit.quiz.services.utils.DateUtils;
import com.workruit.quiz.services.utils.PercentageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Santosh
 *
 */
@Service
@Slf4j
public class UserQuizService {
    private @Autowired UserQuizRepository userQuizRepository;
    private @Autowired UserQuizAnswersRepository userQuizAnswersRepository;
    private @Autowired QuizRepository quizRepository;
    private @Autowired QuestionRepository questionRepository;
    private @Autowired AnswerRepository answerRepository;
    private @Autowired EnterpriseRepository enterpriseRepository;
    private @Autowired QuizTopicListRepository quizTopicListRepository;
    private @Autowired QuizCategoryListRepository quizCategoryListRepository;
    private @Autowired CategoryRepository categoryRepository;
    private @Autowired AnalyticsService analyticsService;
    private @Autowired UserRepository userRepository;
    private @Autowired SubscriptionLimitService subscriptionLimitService;
    private @Autowired EnterpriseService enterpriseService;
    private @Autowired QuestionAnswerService questionAnswerService;
    private ObjectMapper objectMapper = new ObjectMapper();
    private @Autowired QuizService quizService;
    private @Autowired Scheduler scheduler;

    @Transactional
    public Long create(Long userId, Long quizId, String code) throws WorkruitException, Exception {
        try {
            log.debug("Creating quiz:{} for user: {}", quizId, userId);
            if (userQuizRepository.findByUserIdAndQuizId(userId, quizId) != null) {
                throw new WorkruitException("User has already been taken/started the quiz");
            }

            Optional<Quiz> quiz = quizRepository.findByIdAndStatus(quizId, QuizSubmitStatus.ACTIVE);
            if (!quiz.isPresent()) {
                throw new WorkruitException("Quiz doesnt exist with the id provided or Quiz is not ACTIVE yet");
            }

            boolean limit = subscriptionLimitService.checkParticipantsLimit(quiz.get().getEnterpriseId(),
                    quiz.get().getId());
            if (!limit) {
                throw new WorkruitException("Quiz Participants limit exceeded for this quiz");
            }

            if (StringUtils.isBlank(code)) {
                code = null;
            }
            String quizCode = quiz.get().getCode();
            if (StringUtils.isBlank(quizCode)) {
                quizCode = null;
            }
            if (!StringUtils.equalsIgnoreCase(code, quizCode)) {
                throw new WorkruitException("Input code is incorrect");
            }
            UserQuiz userQuiz = new UserQuiz();
            userQuiz.setQuizId(quizId);
            userQuiz.setUserId(userId);
            userQuiz.setStatus(UserQuizStatus.NOT_STARTED);
            userQuiz.setVisible(true);
            userQuiz = userQuizRepository.save(userQuiz);
            analyticsService.updateUserQuizActions(userQuiz.getId());
            return userQuiz.getId();
        } catch (Exception e) {
            log.error("Error while saving user quiz", e);
            throw e;
        }
    }

    public List<UserQuizDetailDTO> myQuizs(Long userId, int pageNumber, int pageSize) throws Exception {
        try {
            PageRequest pageRequest = PageRequest.of(pageNumber, pageSize,Sort.by(Sort.Direction.DESC, "modifiedDate"));
            List<UserQuizDetailDTO> userQuizDetailDTOs = new ArrayList<>();
            List<UserQuiz> userQuizs = userQuizRepository.findByUserId(userId, pageRequest);
            List<QuizSubmitStatus> statuses = new ArrayList<>();
            statuses.add(QuizSubmitStatus.ACTIVE);
            statuses.add(QuizSubmitStatus.CLOSE);
            for (UserQuiz userQuiz : userQuizs) {
                buildUserQuizs(userId, userQuizDetailDTOs, userQuiz, statuses);
            }
            return userQuizDetailDTOs;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<UserQuizDetailDTO> myQuiz(Long userId, Long quizId) throws Exception {
        try {
            List<UserQuizDetailDTO> userQuizDetailDTOs = new ArrayList<>();
            UserQuiz userQuiz = userQuizRepository.findByUserIdAndQuizId(userId, quizId);
            if (userQuiz == null) {
                throw new WorkruitException("Quiz does not taken by the logged in user");
            }
            List<QuizSubmitStatus> statuses = new ArrayList<>();
            statuses.add(QuizSubmitStatus.ACTIVE);
            buildUserQuizs(userId, userQuizDetailDTOs, userQuiz, statuses);
            return userQuizDetailDTOs;
        } catch (Exception e) {
            throw e;
        }
    }

    private void buildUserQuizs(Long userId, List<UserQuizDetailDTO> userQuizDetailDTOs, UserQuiz userQuiz,
                                List<QuizSubmitStatus> statuses) throws WorkruitException, IOException {
        UserQuizDetailDTO userQuizDetailDTO = new UserQuizDetailDTO();
        Quiz quiz = quizRepository.findByIdAndStatusIn(userQuiz.getQuizId(), statuses);
        if (quiz == null) {
            return;
        }
        String name = quiz.getName();
        String level = quiz.getLevel().name();
        long timeInMillis = DateUtils.nextDayStartingTime(quiz.getExpiryDate()).getTime() - new Date().getTime();
        long days = (timeInMillis / (60 * 60 * 24 * 1000));
        long hours = 0;
        long mins = 0;
        if (days == 0) {
            long milliseconds = timeInMillis % (60 * 60 * 24 * 1000);
            hours = milliseconds / (60 * 60 * 1000);
            if (hours == 0) {
                mins = milliseconds / (60 * 1000);
            }
        }
        Long numberOfQuestionsAnswered = userQuizAnswersRepository.countByUserQuizIdAndUserIdWithoutLikeDislike(userQuiz.getId(), userId);
        Long numberOfQuestions = questionRepository.countByQuizIdAndQuestionTypeNot(quiz.getId(), QuestionType.LIKE_OR_DISLIKE);
        if (StringUtils.isNotBlank(quiz.getCode())) {
            userQuizDetailDTO.setVisibility(QuizVisibility.PRIVATE);
        } else {
            userQuizDetailDTO.setVisibility(QuizVisibility.PUBLIC);
        }
        if (StringUtils.isNotBlank(quiz.getQuizTimeLimit())) {
            Long x = null;
            try {
                x = checkAndQuizTimeLimits(userQuiz, quiz);
            } catch (Exception e) {
                // TODO: handle exception
            }
            if (x != null) {
                userQuizDetailDTO.setTimeLeftForQuiz(parseQuizTimeLimits(x));
            }
        }

        if (numberOfQuestionsAnswered == numberOfQuestions || userQuiz.getStatus() == UserQuizStatus.COMPLETED) {
            String status = "";
            String floatStatus = "";
            if (userQuiz.getStatus() == UserQuizStatus.COMPLETED) {
                status = "Completed";
                floatStatus = "100%";
            } else if (userQuiz.getStatus() == UserQuizStatus.PENDING_REVIEW) {
                status = "In Review";
            } else {
                if (numberOfQuestions == 0 && numberOfQuestionsAnswered == 0) {
                    status = "100%";
                } else {
                    status = String.valueOf(
                            PercentageUtils.getCorrectPercentageInInt(numberOfQuestionsAnswered, numberOfQuestions))
                            + "%";
                    floatStatus = String.valueOf(
                            PercentageUtils.getPercentageInDouble(numberOfQuestionsAnswered, numberOfQuestions)) + "%";
                }
            }
            userQuizDetailDTO.setQuizStatus(numberOfQuestionsAnswered + "/" + numberOfQuestions);
            userQuizDetailDTO.setOverallStatus(status);
            userQuizDetailDTO.setOverallStatusFloat(floatStatus);
        } else {
            String status = String.valueOf(PercentageUtils.getCorrectPercentageInInt(numberOfQuestionsAnswered, numberOfQuestions)) + "%";
            String statusFloat = String
                    .valueOf(PercentageUtils.getPercentageInDouble(numberOfQuestionsAnswered, numberOfQuestions)) + "%";
            userQuizDetailDTO.setQuizStatus(numberOfQuestionsAnswered + "/" + numberOfQuestions);
            userQuizDetailDTO.setOverallStatus(status);
            userQuizDetailDTO.setOverallStatusFloat(statusFloat);
        }
        Enterprise enterprise = enterpriseRepository.findById(quiz.getEnterpriseId()).get();
        String enterpriseName = enterprise.getName();
        userQuizDetailDTO.setId(userQuiz.getId());
        userQuizDetailDTO.setName(name);
        userQuizDetailDTO.setLevel(level);
        userQuizDetailDTO.setTopic(getTopicNamesForQuiz(quiz.getId()));
        userQuizDetailDTO.setCategory(getCategoryNamesForQuiz(quiz.getId()));
        userQuizDetailDTO.setDeadline("Deadline - " + (days > 0 ? days : 0) + (days > 1 ? " days" : " day") + " left");
        if (days == 0 && hours > 0) {
            userQuizDetailDTO.setDeadline("Deadline - " + hours + (hours > 1 ? " hours" : " hour") + " left");
        } else if (days == 0 && hours == 0 & mins > 0) {
            userQuizDetailDTO.setDeadline("Deadline - " + mins + (mins > 1 ? " mins" : " min") + " left");
        } else if (days <= 0) {
            userQuizDetailDTO.setDeadline("Expired");
        }
        userQuizDetailDTO.setPostedBy(enterpriseName);
        userQuizDetailDTO.setQuizId(quiz.getId());
        userQuizDetailDTO.setNumberOfParticipants(userQuizRepository.countByQuizId(quiz.getId()));
        userQuizDetailDTO.setQuizPostedDate(DateUtils.format(quiz.getCreatedDate()));

        if(enterprise != null){
            userQuizDetailDTO.setEnterpriseId(enterprise.getId());
            if(enterprise.getLogo() != null && !enterprise.getLogo().equals("")){
                userQuizDetailDTO.setEnterpriseImageKey(enterprise.getLogo());
                userQuizDetailDTO.setEnterpriseImage(enterpriseService.getImage(enterprise.getLogo()));
            }
        }
        if (StringUtils.isNotBlank(quiz.getCode())) {
            userQuizDetailDTO.setVisibility(QuizVisibility.PRIVATE);
        } else {
            userQuizDetailDTO.setVisibility(QuizVisibility.PUBLIC);
        }
        userQuizDetailDTO.setTimedOut(userQuiz.isQuizTimedOut());
        userQuizDetailDTO.setDeadlineDate(DateUtils.format(quiz.getExpiryDate()));
        userQuizDetailDTO.setSubmitStatus(quiz.getStatus());
        userQuizDetailDTO.setTimeLimitForQuiz(quiz.getQuizTimeLimit());
        userQuizDetailDTO.setQuizSubmittedIn(userQuiz.getQuizSubmittedIn());
        userQuizDetailDTOs.add(userQuizDetailDTO);
    }

    public PageApiResponse filterWithPagination(Long enterpriseId, List<Long> categoryIds, int pageNumber, int pageSize,
                                                Long userId) throws Exception {
        try {
            Long numberOfRecords = 0L;
            List<UserQuizDetailDTO> userQuizDetailDTOs = new ArrayList<>();
            List<QuizSubmitStatus> statuses = new ArrayList<>();
            statuses.add(QuizSubmitStatus.ACTIVE);
            // statuses.add(QuizSubmitStatus.CLOSE);
            List<Long> quizIds = null;
            User user = userRepository.findById(userId).get();
            if (enterpriseId != null) {
                if (!CollectionUtils.isEmpty(categoryIds)) {
                    if (user.getAccessCode() == null) {
                        quizIds = quizRepository.filterQuizByEnterpriseAndCategoryIds(enterpriseId, categoryIds,
                                pageNumber * pageSize, pageSize, userId);
                        numberOfRecords = quizRepository.countByfilterQuizByEnterpriseAndCategoryIds(enterpriseId,
                                categoryIds, userId);
                    } else {
                        //TODO need to optimise the query
                        if (user.getAccessCode().contains(",")) {
                            Long numberOfRecordsTemp = 0L;
                            List<Long> quizIdsTemp = null;
                            quizIds = new ArrayList<Long>();

                            List<String> accessCodes = Arrays.asList(user.getAccessCode().split(","));

                            quizIdsTemp = quizRepository.filterQuizByEnterpriseAndCategoryIdsWithEnterpriseType(
                                    enterpriseId, categoryIds, pageNumber * pageSize, pageSize, userId, accessCodes);
                            numberOfRecordsTemp = quizRepository
                                    .countByfilterQuizByEnterpriseAndCategoryIdsByEnterpriseType(enterpriseId,
                                            categoryIds, userId, accessCodes);
                            quizIds.addAll(quizIdsTemp);
                            numberOfRecords = numberOfRecords + numberOfRecordsTemp;

                        } else {
                            quizIds = quizRepository.filterQuizByEnterpriseAndCategoryIdsWithEnterpriseType(
                                    enterpriseId, categoryIds, pageNumber * pageSize, pageSize, userId,
                                    Arrays.asList(user.getAccessCode()));
                            numberOfRecords = quizRepository
                                    .countByfilterQuizByEnterpriseAndCategoryIdsByEnterpriseType(enterpriseId,
                                            categoryIds, userId, Arrays.asList(user.getAccessCode()));
                        }
                    }
                } else {
                    quizIds = quizRepository.filterQuizByEnterprise(enterpriseId, pageNumber * pageSize, pageSize,
                            userId);
                    numberOfRecords = quizRepository.countByfilterQuizByEnterprise(enterpriseId, userId);
                }
            } else {
                if (!CollectionUtils.isEmpty(categoryIds)) {
                    if (user.getAccessCode() == null) {
                        quizIds = quizRepository.filterQuizByCategoryIds(categoryIds, pageNumber * pageSize, pageSize,
                                userId);
                        numberOfRecords = quizRepository.countByfilterQuizByCategoryIds(categoryIds, userId);
                    } else {
                        quizIds = quizRepository.filterQuizByCategoryIdsByEnterpriseType(categoryIds,
                                pageNumber * pageSize, pageSize, userId, user.getAccessCode());
                        numberOfRecords = quizRepository.countByfilterQuizByCategoryIdsByEnterpriseType(categoryIds,
                                userId, user.getAccessCode());
                    }
                } else {
                    if (user.getAccessCode() == null) {
                        quizIds = quizRepository.filterQuiz(pageNumber * pageSize, pageSize, userId);
                        numberOfRecords = quizRepository.countByfilterQuiz(userId);
                    } else {
                        quizIds = quizRepository.filterQuizWithEnterpriseType(pageNumber * pageSize, pageSize, userId,
                                user.getAccessCode());
                        numberOfRecords = quizRepository.countByfilterQuizWithEnterpriseType(userId,
                                user.getAccessCode());
                    }
                }
            }
            for (Quiz quiz : quizRepository.findByIdInAndStatus(quizIds, QuizSubmitStatus.ACTIVE)) {
                UserQuiz userQuiz = userQuizRepository.findByUserIdAndQuizId(userId, quiz.getId());
                if (userQuiz != null) {
                    buildUserQuizs(userId, userQuizDetailDTOs, userQuiz, statuses);
                    continue;
                }
                UserQuizDetailDTO userQuizDetailDTO = new UserQuizDetailDTO();
                String name = quiz.getName();
                String level = quiz.getLevel().name();
                long timeInMillis = DateUtils.nextDayStartingTime(quiz.getExpiryDate()).getTime()
                        - new Date().getTime();
                long days = (timeInMillis / (60 * 60 * 24 * 1000));
                long hours = 0;
                long mins = 0;
                if (days == 0) {
                    long milliseconds = timeInMillis % (60 * 60 * 24 * 1000);
                    hours = milliseconds / (60 * 60 * 1000);
                    if (hours == 0) {
                        mins = milliseconds / (60 * 1000);
                    }
                }

                Long numberOfQuestions = questionRepository.countByQuizId(quiz.getId());
                userQuizDetailDTO.setQuizStatus("0/" + numberOfQuestions);
                Enterprise enterprise = enterpriseRepository.findById(quiz.getEnterpriseId()).get();
                String enterpriseName = enterprise.getName();
                userQuizDetailDTO.setId(-1L);
                userQuizDetailDTO.setName(name);
                userQuizDetailDTO.setLevel(level);
                userQuizDetailDTO.setTopic(getTopicNamesForQuiz(quiz.getId()));
                userQuizDetailDTO.setCategory(getCategoryNamesForQuiz(quiz.getId()));
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
                    userQuizDetailDTO.setCategories(categoryMap.entrySet().stream().map(entry -> {
                        CategoryListDTO categoryListDTO = new CategoryListDTO();
                        categoryListDTO.setId(entry.getKey().getId());
                        categoryListDTO.setName(entry.getKey().getName());
                        categoryListDTO.setTopics(entry.getValue());
                        return categoryListDTO;
                    }).collect(Collectors.toList()));
                });
                userQuizDetailDTO.setDeadline("Deadline - " + (days > 0 ? days : 0) + (days > 1 ? " days" : " day") + " left");
                if (days == 0 && hours > 0) {
                    userQuizDetailDTO.setDeadline("Deadline - " + hours + (hours > 1 ? " hours" : " hour") + " left");
                } else if (days == 0 && hours == 0 && mins > 0) {
                    userQuizDetailDTO.setDeadline("Deadline - " + mins + (mins > 1 ? " mins" : " min") + " left");
                } else if (days <= 0) {
                    userQuizDetailDTO.setDeadline("Expired");
                }

                userQuizDetailDTO.setPostedBy(enterpriseName);
                userQuizDetailDTO.setQuizId(quiz.getId());
                userQuizDetailDTO.setOverallStatus("Not Started");
                userQuizDetailDTO.setNumberOfParticipants(userQuizRepository.countByQuizId(quiz.getId()));
                userQuizDetailDTO.setQuizPostedDate(DateUtils.format(quiz.getCreatedDate()));
                userQuizDetailDTO.setSubmitStatus(quiz.getStatus());

                if(enterprise != null){
                    userQuizDetailDTO.setEnterpriseId(enterprise.getId());
                    if(enterprise.getLogo() != null && !enterprise.getLogo().equals("")){
                        userQuizDetailDTO.setEnterpriseImageKey(enterprise.getLogo());
                        userQuizDetailDTO.setEnterpriseImage(enterpriseService.getImage(enterprise.getLogo()));
                    }
                }

                if (StringUtils.isNotBlank(quiz.getCode())) {
                    userQuizDetailDTO.setVisibility(QuizVisibility.PRIVATE);
                } else {
                    userQuizDetailDTO.setVisibility(QuizVisibility.PUBLIC);
                }
                userQuizDetailDTO.setTimeLimitForQuiz(quiz.getQuizTimeLimit());
                userQuizDetailDTOs.add(userQuizDetailDTO);
            }
            PageApiResponse pageApiResponse = new PageApiResponse();
            pageApiResponse.setData(userQuizDetailDTOs);
            pageApiResponse.setNumberOfRecords(numberOfRecords);
            pageApiResponse.setPage(pageNumber);
            pageApiResponse.setSize(pageSize);
            return pageApiResponse;
        } catch (Exception e) {
            log.error("Error while saving the quiz", e);
            throw e;
        }
    }

    /**
     * This method filter quizs based on enterprise id and topic ids.
     *
     * @param userId
     * @param enterpriseId
     * @param categoryIds
     * @return
     * @throws Exception
     */
    public List<UserQuizDetailDTO> filter(Long userId, Long enterpriseId, List<Long> categoryIds) throws Exception {
        try {
            // All User taken Quizzes
            List<UserQuizDetailDTO> userQuizDetailDTOs = myQuizs(userId, 0, Integer.MAX_VALUE);
            // Filtered Quizzes by enterpriseId and categories
            List<QuizDTO> quizDTOs = quizService.filter(enterpriseId, categoryIds, userId);
            // All filtered quizIds
            List<Long> quizIdsTemp = quizDTOs.stream().map(dto -> dto.getId()).collect(Collectors.toList());

            // Filter based on topicIds
            final List<Long> quizIds = new ArrayList<>();
            if (!CollectionUtils.isEmpty(categoryIds)) {
                quizIds.addAll(quizTopicListRepository.findByQuizIdInAndTopicIdIn(quizIdsTemp, categoryIds).stream()
                        .map(x -> x.getQuizId()).collect(Collectors.toList()));
            } else {
                quizIds.addAll(quizTopicListRepository.findByQuizIdIn(quizIdsTemp).stream().map(x -> x.getQuizId())
                        .collect(Collectors.toList()));
            }
            // Filter User taken quizes based on quizIds
            userQuizDetailDTOs = userQuizDetailDTOs.stream().filter(dto -> quizIds.contains(dto.getQuizId()))
                    .collect(Collectors.toList());
            //
            List<Long> filterQuizIds = userQuizDetailDTOs.stream().map(dto -> dto.getQuizId())
                    .collect(Collectors.toList());
            quizIds.removeAll(filterQuizIds);
            for (Quiz quiz : quizRepository.findByIdInAndStatus(quizIds, QuizSubmitStatus.ACTIVE)) {
                UserQuizDetailDTO userQuizDetailDTO = new UserQuizDetailDTO();
                String name = quiz.getName();
                String level = quiz.getLevel().name();
                long timeInMillis = DateUtils.nextDayStartingTime(quiz.getExpiryDate()).getTime()
                        - new Date().getTime();
                long days = (timeInMillis / (60 * 60 * 24 * 1000));
                long hours = 0;
                long mins = 0;
                if (days == 0) {
                    long milliseconds = timeInMillis % (60 * 60 * 24 * 1000);
                    hours = milliseconds / (60 * 60 * 1000);
                    if (hours == 0) {
                        mins = milliseconds / (60 * 1000);
                    }
                }

                Long numberOfQuestions = questionRepository.countByQuizId(quiz.getId());
                userQuizDetailDTO.setQuizStatus("0/" + numberOfQuestions);
                Enterprise enterprise = enterpriseRepository.findById(quiz.getEnterpriseId()).get();
                String enterpriseName = enterprise.getName();
                userQuizDetailDTO.setId(-1L);
                userQuizDetailDTO.setName(name);
                userQuizDetailDTO.setLevel(level);
                userQuizDetailDTO.setTopic(getTopicNamesForQuiz(quiz.getId()));
                userQuizDetailDTO.setCategory(getCategoryNamesForQuiz(quiz.getId()));
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
                    userQuizDetailDTO.setCategories(categoryMap.entrySet().stream().map(entry -> {
                        CategoryListDTO categoryListDTO = new CategoryListDTO();
                        categoryListDTO.setId(entry.getKey().getId());
                        categoryListDTO.setName(entry.getKey().getName());
                        categoryListDTO.setTopics(entry.getValue());
                        return categoryListDTO;
                    }).collect(Collectors.toList()));
                });
                userQuizDetailDTO.setDeadline("Deadline - " + (days > 0 ? days : 0) + (days > 1 ? " days" : " day") +" left");
                if (days == 0 && hours > 0) {
                    userQuizDetailDTO.setDeadline("Deadline - " + hours + (hours > 1 ? " hours" : " hour") + " left");
                } else if (days == 0 && hours == 0 && mins > 0) {
                    userQuizDetailDTO.setDeadline("Deadline - " + mins + (mins > 1 ? " mins" : " min") + " left");
                } else if (days <= 0) {
                    userQuizDetailDTO.setDeadline("Expired");
                }

                userQuizDetailDTO.setPostedBy(enterpriseName);
                userQuizDetailDTO.setQuizId(quiz.getId());
                userQuizDetailDTO.setOverallStatus("Not Started");
                userQuizDetailDTO.setNumberOfParticipants(userQuizRepository.countByQuizId(quiz.getId()));
                userQuizDetailDTO.setQuizPostedDate(DateUtils.format(quiz.getCreatedDate()));

                if(enterprise != null){
                    userQuizDetailDTO.setEnterpriseId(enterprise.getId());
                    if(enterprise.getLogo() != null && !enterprise.getLogo().equals("")){
                        userQuizDetailDTO.setEnterpriseImageKey(enterprise.getLogo());
                        userQuizDetailDTO.setEnterpriseImage(enterpriseService.getImage(enterprise.getLogo()));
                    }
                }

                if (StringUtils.isNotBlank(quiz.getCode())) {
                    userQuizDetailDTO.setVisibility(QuizVisibility.PRIVATE);
                } else {
                    userQuizDetailDTO.setVisibility(QuizVisibility.PUBLIC);
                }
                userQuizDetailDTOs.add(userQuizDetailDTO);
            }
            return userQuizDetailDTOs;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public PageApiResponse listQuizs(int page, int size) {
        try {
            PageApiResponse pageApiResponse = new PageApiResponse();
            List<UserQuizDetailDTO> list = new ArrayList<>();
            Date date = new Date();
            List<Quiz> quizs = quizRepository.findByStatus(QuizSubmitStatus.ACTIVE,
                    PageRequest.of(page, size, Sort.by(Direction.DESC, "createdDate")));
            buildQuizs(list, quizs, date);
            pageApiResponse.setData(list);
            pageApiResponse.setPage(page);
            pageApiResponse.setSize(size);
            pageApiResponse.setNumberOfRecords(quizRepository.countByStatus(QuizSubmitStatus.ACTIVE));
            return pageApiResponse;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Top quizzes include all quizzes which are ACTIVE AND CLOSE.
     *
     * @param userId
     * @return
     * @throws Exception
     */
    public List<UserQuizDetailDTO> topQuizs(Long userId) throws Exception {
        try {// Find top
            User user = userRepository.findById(userId).get();
            List<UserQuizDetailDTO> userQuizDetailDTOs = myQuizs(userId, 0, Integer.MAX_VALUE);
            List<Quiz> findByIdNotIn = null;
            Date date = new Date();
            if (!CollectionUtils.isEmpty(userQuizDetailDTOs)) {
                List<Long> ids = userQuizDetailDTOs.stream().map(dto -> dto.getQuizId()).collect(Collectors.toList());
                if (StringUtils.isNotBlank(user.getAccessCode())) {
                    findByIdNotIn = quizRepository.findByIdNotInAndStatusByEnterpriseCodeLimit(
                            QuizSubmitStatus.ACTIVE.name(), user.getAccessCode(), ids);
                } else {
                    findByIdNotIn = quizRepository
                            .findByIdNotInAndStatusByEnterprisePublicLimit(QuizSubmitStatus.ACTIVE.name(), ids);
                }
            } else {
                if (StringUtils.isNotBlank(user.getAccessCode())) {
                    findByIdNotIn = quizRepository.findByStatusByEnterpriseCodeLimit(QuizSubmitStatus.ACTIVE.name(),
                            user.getAccessCode());
                } else {
                    findByIdNotIn = quizRepository
                            .findByStatusByEnterpriseTypePublicLimit(QuizSubmitStatus.ACTIVE.name());
                }
            }
            // Remove the quizzes which are in progress and not started and expired.
            userQuizDetailDTOs = new ArrayList<>();
            buildQuizs(userQuizDetailDTOs, findByIdNotIn, date);
            Iterator<UserQuizDetailDTO> iter = userQuizDetailDTOs.iterator();
            while (iter.hasNext()) {
                UserQuizDetailDTO userQuizDetailDTO = iter.next();
                long t = DateUtils.getDate(userQuizDetailDTO.getDeadlineDate()).getTime() - date.getTime();
                if (t < 0) {
                    iter.remove();
                }
            }
            userQuizDetailDTOs.forEach(dto -> {
                Optional<Enterprise> optional = enterpriseRepository
                        .findById(quizRepository.findById(dto.getQuizId()).get().getEnterpriseId());
                if (optional.isPresent()) {
                    String imageKey = optional.get().getLogo();
                    try {
                        if (StringUtils.isNotBlank(imageKey)) {
                            dto.setEnterpriseImage(enterpriseService.getImage(imageKey));
                            dto.setEnterpriseImageKey(imageKey);
                        }
                        dto.setEnterpriseId(optional.get().getId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return userQuizDetailDTOs;
        } catch (Exception e) {
            log.error("Error while getting top quizs", e);
            throw e;
        }
    }

    private void buildQuizs(List<UserQuizDetailDTO> userQuizDetailDTOs, List<Quiz> findByIdNotIn, Date date) {
        for (Quiz quizDTO : findByIdNotIn) {
            UserQuizDetailDTO userQuizDetailDTO = new UserQuizDetailDTO();
            Optional<Quiz> optionalQuiz = quizRepository.findByIdAndStatus(quizDTO.getId(), QuizSubmitStatus.ACTIVE);
            if (optionalQuiz.isPresent() && (optionalQuiz.get().getExpiryDate().getTime() - date.getTime()) > 0) {
                Quiz quiz = optionalQuiz.get();
                String name = quiz.getName();
                String level = quiz.getLevel().name();
                long timeInMillis = DateUtils.nextDayStartingTime(quiz.getExpiryDate()).getTime()
                        - new Date().getTime();
                long days = (timeInMillis / (60 * 60 * 24 * 1000));
                long hours = 0;
                if (days == 0) {
                    long milliseconds = timeInMillis % (60 * 60 * 24 * 1000);
                    hours = milliseconds / (60 * 60 * 1000);
                }

                Long numberOfQuestions = questionRepository.countByQuizId(quiz.getId());
                userQuizDetailDTO.setQuizStatus("0/" + numberOfQuestions);
                Enterprise enterprise = enterpriseRepository.findById(quiz.getEnterpriseId()).get();
                String enterpriseName = enterprise.getName();
                userQuizDetailDTO.setId(-1L);
                userQuizDetailDTO.setName(name);
                userQuizDetailDTO.setLevel(level);
                userQuizDetailDTO.setTopic(getTopicNamesForQuiz(quiz.getId()));
                userQuizDetailDTO.setCategory(getCategoryNamesForQuiz(quiz.getId()));
                userQuizDetailDTO.setDeadline("Deadline - " + (days > 0 ? days : 0) + (days > 1 ? " days" : " day") + " left");
                if (days == 0 && hours > 0) {
                    userQuizDetailDTO.setDeadline("Deadline - " + hours + (hours > 1 ? " hours" : " hour") + " left");
                } else if (days <= 0) {
                    userQuizDetailDTO.setDeadline("Expired");
                }
                userQuizDetailDTO.setPostedBy(enterpriseName);
                userQuizDetailDTO.setQuizId(quiz.getId());
                userQuizDetailDTO.setOverallStatus("Not Started");
                userQuizDetailDTO.setNumberOfParticipants(userQuizRepository.countByQuizId(quiz.getId()));
                userQuizDetailDTO.setQuizPostedDate(DateUtils.format(quiz.getCreatedDate()));
                userQuizDetailDTO.setDeadlineDate(DateUtils.format(quiz.getExpiryDate()));
                if (StringUtils.isNotBlank(quiz.getCode())) {
                    userQuizDetailDTO.setVisibility(QuizVisibility.PRIVATE);
                } else {
                    userQuizDetailDTO.setVisibility(QuizVisibility.PUBLIC);
                }
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
                    userQuizDetailDTO.setCategories(categoryMap.entrySet().stream().map(entry -> {
                        CategoryListDTO categoryListDTO = new CategoryListDTO();
                        categoryListDTO.setId(entry.getKey().getId());
                        categoryListDTO.setName(entry.getKey().getName());
                        categoryListDTO.setTopics(entry.getValue());
                        return categoryListDTO;
                    }).collect(Collectors.toList()));
                });
                userQuizDetailDTO.setTimeLimitForQuiz(quiz.getQuizTimeLimit());
                // checkAndQuizTimeLimits(user)
                // userQuizDetailDTO.setTimeLeftForQuiz(numberOfQuestions);

                userQuizDetailDTOs.add(userQuizDetailDTO);
            } else {
                log.info("Not a valid quiz obj");
            }
        }
    }

    public long countByUserIdAndUserquizId(Long userId, Long userQuizId) {
        UserQuiz userQuiz = userQuizRepository.findByIdAndUserId(userQuizId, userId);
        return questionRepository.countByQuizId(userQuiz.getQuizId());
    }

    public long countByUserId(Long userId) {
        return userQuizRepository.countByUserId(userId);
    }

    @Transactional
    public List<UserQuizQuestionAnswerDTO> start(Long userId, Long userQuizId, int pageNumber, int size)
            throws Exception {
        try {
            Timestamp quizStartTime = new Timestamp(new Date().getTime());
            UserQuiz userQuiz = userQuizRepository.findByIdAndUserId(userQuizId, userId);
            if (userQuiz == null) {
                throw new WorkruitException("Quiz is not created/cannot be restarted");
            }
            boolean restartQuiz = true;
            if (userQuiz.getQuizStartTime() == null) {
                userQuiz.setQuizStartTime(quizStartTime);
                restartQuiz = false;
            }
            Quiz quiz = quizRepository.findById(userQuiz.getQuizId()).get();
            Long timeLeft = checkAndQuizTimeLimits(userQuiz, quiz);
            if (timeLeft == null) {
                throw new WorkruitException("Quiz already timed out");
            }
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("quizId", userQuiz.getQuizId());
            jobDataMap.put("userId", userId);
            jobDataMap.put("userQuizId", userQuiz.getId());
            JobDetail job = JobBuilder.newJob(UserQuizTimeoutTask.class).setJobData(jobDataMap).build();
            String timeLimit = quiz.getQuizTimeLimit();
            if (StringUtils.isNotBlank(timeLimit)
                    && !StringUtils.equalsIgnoreCase(quiz.getQuizTimeLimit(), "00:00:00") && !restartQuiz) {
                String[] limits = timeLimit.split(":");
                long hourTime = limits[0] != null && limits[0].length() > 0 && !StringUtils.equals(limits[0], "00")
                        ? (Long.parseLong(limits[0].charAt(0) == '0' ? String.valueOf(limits[0].charAt(1)) : limits[0])
                        * 60 * 60 * 1000)
                        : 0;
                long minTime = limits[1] != null && limits[1].length() > 0 && !StringUtils.equals(limits[1], "00")
                        ? (Long.parseLong(limits[1].charAt(0) == '0' ? String.valueOf(limits[1].charAt(1)) : limits[1])
                        * 60 * 1000)
                        : 0;
                long secTime = limits[2] != null && limits[2].length() > 0 && !StringUtils.equals(limits[2], "00")
                        ? (Long.parseLong(limits[2].charAt(0) == '0' ? String.valueOf(limits[2].charAt(1)) : limits[2])
                        * 1000)
                        : 0;
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.MILLISECOND, (int) (hourTime + minTime + secTime));
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("UserQuizTimeout:" + userQuiz.getId().toString(), "jobs").startAt(cal.getTime())
                        .build();
                scheduler.scheduleJob(job, trigger);
            }

            PageRequest pageRequest = PageRequest.of(pageNumber, size);
            List<Question> questions = questionRepository.findByQuizId(userQuiz.getQuizId(), pageRequest);
            List<UserQuizQuestionAnswerDTO> questionDTOs = questions.stream().map(question -> {
                try {
                    Map questionObj = objectMapper.readValue(question.getQuestion().replace("\n",""), Map.class);
                    log.info("Question has options:" + question.getOptions());
                    List list = question.getOptions() != null
                            ? objectMapper.readValue(question.getOptions().replace("\n",""), List.class)
                            : null;
                    UserQuizAnswers userQuizAnswers = userQuizAnswersRepository
                            .findByUserQuizIdAndUserIdAndQuestionId(userQuizId, userId, question.getId());
                    UserQuizQuestionAnswerDTO userQuizQuestionAnswerDTO = new UserQuizQuestionAnswerDTO();
                    if (userQuizAnswers != null) {
                        userQuizQuestionAnswerDTO.setQuestionAnswered(true);
                        userQuizQuestionAnswerDTO
                                .setOption(objectMapper.readValue(userQuizAnswers.getOption().replace("\n",""), Map.class));
                    } else {
                        userQuizQuestionAnswerDTO.setQuestionAnswered(false);
                    }
                    userQuizQuestionAnswerDTO.setQuestionObj(new QuestionObjectDTO(
                            String.valueOf(questionObj.get("question")), String.valueOf(questionObj.get("url")), String.valueOf(questionObj.get("explanation")), String.valueOf(questionObj.get("explanationImage"))));
                    userQuizQuestionAnswerDTO.setQuestionId(question.getId());
                    userQuizQuestionAnswerDTO.setQuestionType(question.getQuestionType());
                    userQuizQuestionAnswerDTO.setOptions(list);
                    return userQuizQuestionAnswerDTO;
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            userQuiz.setStatus(UserQuizStatus.IN_PROGRESS);
            userQuizRepository.save(userQuiz);
            analyticsService.updateUserQuizActions(userQuiz.getId());
            return questionDTOs;
        } catch (Exception e) {
            log.error("Error while saving user quiz", e);
            throw e;
        }
    }
    private long parseQuizTimeLimit(String quizTimeLimit) {
        String[] parts = quizTimeLimit.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return hours * 3600 + minutes * 60 + seconds;
    }

    @SuppressWarnings("rawtypes")
    @Transactional
    public UserQuizResponseDTO submit(Long userQuizId, Long userId,boolean autoSubmit) throws Exception {
        try {
            UserQuiz userQuiz = userQuizRepository.findByIdAndUserId(userQuizId, userId);
            if (userQuiz == null) {
                throw new WorkruitException("Quiz cannot be submitted by this user");
            }
            List<Question> questions = questionRepository.findByQuizId(userQuiz.getQuizId());
            Map<Long, Boolean> resultMap = new HashMap<>();
            List<QuizSubmitStatus> multipleStatus = new ArrayList<>();
            multipleStatus.add(QuizSubmitStatus.ACTIVE);

            Quiz quiz = quizRepository.findByIdAndStatusIn(userQuiz.getQuizId(), multipleStatus);
            if (quiz == null) {
                throw new WorkruitException("Quiz does not exist or quiz is expired");
            }

            Long timeLeft = checkAndQuizTimeLimits(userQuiz, quiz);
            if (timeLeft == null || autoSubmit) {
                userQuiz.setQuizTimedOut(true);
            }
            else{
                Long quizTimeLimit = parseQuizTimeLimit(quiz.getQuizTimeLimit());
                userQuiz.setQuizSubmittedIn(parseQuizTimeLimits(quizTimeLimit - timeLeft));
            }
            /*
             * if (timeLeft == null) { handleQuizTimeComplete(userQuizId, userId, quiz); //
             * throw new WorkruitException("Quiz time limit exceeded"); }
             */
            if(userQuiz.getStatus() == UserQuiz.UserQuizStatus.IN_PROGRESS) {
                long unansweredCount = 0;
                long likeDislikeCount = 0;
                long likeCount = 0;
                long dislikeCount = 0;
                for (Question question : questions) {
                    Answer answer = answerRepository.findByQuestionId(question.getId());
                    UserQuizAnswers userAnswer = userQuizAnswersRepository
                            .findByUserQuizIdAndUserIdAndQuestionId(userQuizId, userId, question.getId());
                    if (userAnswer == null || StringUtils.isBlank(userAnswer.getOption())) {
                        if (question.getQuestionType() == QuestionType.LIKE_OR_DISLIKE) {
                            likeDislikeCount++;
                            resultMap.put(question.getId(), null);
                        }
                        else {
                            resultMap.put(question.getId(), false);
                            unansweredCount++;
                        }
                        continue;
                    }
                    if (question.getQuestionType() == QuestionType.TRUE_OR_FALSE
                            || question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                        AnswerDTO output = objectMapper.readValue(answer.getOption(), AnswerDTO.class);
                        Object answerOption = output.getOptions().get(0).getOption();
                        AnswerDTO userOutput = objectMapper.readValue(userAnswer.getOption(), AnswerDTO.class);
                        log.info("Processing submit for questioId:{}", question.getId());
                        Object userAnswerOption = userOutput.getOptions().get(0).getOption();
                        if (answerOption != null && userAnswerOption != null) {
                            if (StringUtils.equalsIgnoreCase(answerOption.toString(), userAnswerOption.toString())) {
                                resultMap.put(question.getId(), true);
                            }
                            else {
                                resultMap.put(question.getId(), false);
                            }
                        }
                    }
                    else if (question.getQuestionType() == QuestionType.DROPDOWN
                            || question.getQuestionType() == QuestionType.CHECKBOX) {
                        log.info("Processing submit for questioId:{}", question.getId());
                        AnswerDTO outputList = objectMapper.readValue(answer.getOption(), AnswerDTO.class);
                        AnswerDTO userOutputList = objectMapper.readValue(userAnswer.getOption(), AnswerDTO.class);
                        Set<String> outputSet = new HashSet<>();
                        Set<String> userOutputSet = new HashSet<>();
                        for (AnswerOptionDTO obj : outputList.getOptions()) {
                            outputSet.add(obj.getOption().toString());
                        }
                        for (AnswerOptionDTO obj : userOutputList.getOptions()) {
                            userOutputSet.add(obj.getOption().toString());
                        }
                        if (outputSet.equals(userOutputSet)) {
                            resultMap.put(question.getId(), true);
                        }
                        else {
                            resultMap.put(question.getId(), false);
                        }
                    }
                    else if (question.getQuestionType() == QuestionType.INTEGER) {
                        AnswerDTO output = objectMapper.readValue(answer.getOption(), AnswerDTO.class);
                        Object answerOption = output.getOptions().get(0).getOption();
                        if (StringUtils.isNotBlank(userAnswer.getOption())) {
                            AnswerDTO userOutput = objectMapper.readValue(userAnswer.getOption(), AnswerDTO.class);
                            Object userAnswerOption = userOutput.getOptions().get(0).getOption();
                            if (StringUtils.equals(answerOption.toString(), userAnswerOption.toString())) {
                                resultMap.put(question.getId(), true);
                            }
                            else {
                                resultMap.put(question.getId(), false);
                            }
                        }
                        else {
                            resultMap.put(question.getId(), false);
                        }
                    }
                    else if (question.getQuestionType() == QuestionType.LIKE_OR_DISLIKE) {
                        AnswerDTO userOutput = objectMapper.readValue(userAnswer.getOption(), AnswerDTO.class);
                        Object userAnswerOption = userOutput.getOptions().get(0).getOption();
                        if (userAnswerOption.toString().equalsIgnoreCase(AnswerOption.LIKE.toString())) {
                            likeCount++;
                        }
                        else if (userAnswerOption.toString().equalsIgnoreCase(AnswerOption.DISLIKE.toString())) {
                            dislikeCount++;
                        }
                        likeDislikeCount++;
                        resultMap.put(question.getId(), null);
                    }
                    else {
                        resultMap.put(question.getId(), null);
                    }
                }
                ObjectMapper objectMapper = new ObjectMapper();
                if (questions.size() == resultMap.size()) {
                    long total = resultMap.size() - likeDislikeCount;
                    long actualTotal = resultMap.values().stream().filter(x -> x != null).count();
                    long correctCount = resultMap.values().stream()
                            .filter(x -> x != null && x.booleanValue() == Boolean.TRUE.booleanValue()).count();
                    long inCorrectCount = resultMap.values().stream()
                            .filter(x -> x != null && x.booleanValue() == Boolean.FALSE.booleanValue()).count();
                    if (total == actualTotal) {
                        userQuiz.setResult(UserQuizResult.PASSED);
                        userQuiz.setStatus(UserQuizStatus.COMPLETED);
                        userQuiz.setResultDataFloat((float) PercentageUtils.getPercentageInDouble(correctCount, total));
                        userQuiz.setResultData((int) Math.round(userQuiz.getResultDataFloat()));
                    }
                    else {
                        userQuiz.setStatus(UserQuizStatus.PENDING_REVIEW);
                    }
                    UserQuizStatusContent userQuizStatusContent = new UserQuizStatusContent();
                    userQuizStatusContent.setCorrectAnswersCount(correctCount);
                    userQuizStatusContent.setInCorrectAnswersCount(inCorrectCount - unansweredCount);
                    userQuizStatusContent.setUnansweredCount(unansweredCount);
                    userQuizStatusContent.setReviewAnswersCount(total - correctCount - inCorrectCount);
                    userQuizStatusContent.setNumberOfQuestions(questions.size());
                    userQuizStatusContent.setAnswersStatus(resultMap);
                    userQuizStatusContent.setLikeDislikeCount(likeDislikeCount);
                    userQuizStatusContent.setLikeCount(likeCount);
                    userQuizStatusContent.setDisLikeCount(dislikeCount);
                    userQuiz.setResultContent(objectMapper.writeValueAsString(userQuizStatusContent));
                    userQuizRepository.save(userQuiz);
                }
                else {
                    long correctCount = resultMap.values().stream()
                            .filter(x -> x.booleanValue() == Boolean.TRUE.booleanValue()).count();
                    userQuiz.setStatus(UserQuizStatus.PENDING_REVIEW);
                    long inCorrectCount = resultMap.values().stream()
                            .filter(x -> x.booleanValue() == Boolean.FALSE.booleanValue()).count();
                    UserQuizStatusContent userQuizStatusContent = new UserQuizStatusContent();
                    userQuizStatusContent.setCorrectAnswersCount(correctCount);
                    userQuizStatusContent.setInCorrectAnswersCount(inCorrectCount - unansweredCount);
                    userQuizStatusContent.setUnansweredCount(unansweredCount);
                    userQuizStatusContent.setReviewAnswersCount(questions.size() - correctCount - inCorrectCount);
                    userQuizStatusContent.setNumberOfQuestions(questions.size());
                    userQuizStatusContent.setAnswersStatus(resultMap);
                    userQuizStatusContent.setLikeDislikeCount(likeDislikeCount);
                    userQuiz.setResultContent(objectMapper.writeValueAsString(userQuizStatusContent));
                    userQuizRepository.save(userQuiz);
                }
                UserQuizResponseDTO userQuizResponseDTO = new UserQuizResponseDTO();
                userQuizResponseDTO.setUserQuizId(userQuizId);
                userQuizResponseDTO.setStatus(userQuiz.getStatus());
                userQuizResponseDTO.setResult(userQuiz.getResultData());
                userQuizResponseDTO.setQuizResult(userQuiz.getResult());
                userQuizResponseDTO.setFloatResult(userQuiz.getResultDataFloat());
                analyticsService.updateUserQuizActions(userQuizId);
                return userQuizResponseDTO;
            } else {
                userQuizRepository.save(userQuiz);
                return null;
            }
        } catch (Exception e) {
            log.error("Error while submitting the quiz", e);
            throw e;
        }

    }

    @Transactional
    public Long update(Long questionId, Long userQuizId, Long userId, Object object) throws Exception {
        try {
            log.debug("USer updating answer for question:{}, userQuiz:{} ", questionId, userQuizId);
            UserQuiz userQuiz = userQuizRepository.findByIdAndUserId(userQuizId, userId);
            List<QuizSubmitStatus> multipleStatus = new ArrayList<>();
            multipleStatus.add(QuizSubmitStatus.ACTIVE);
            Quiz quiz = quizRepository.findByIdAndStatusIn(userQuiz.getQuizId(), multipleStatus);
            if (quiz == null) {
                throw new WorkruitException("Quiz does not exist or quiz is expired");
            }
            Long timeLeft = checkAndQuizTimeLimits(userQuiz, quiz);
            if (timeLeft == null) {
                handleQuizTimeComplete(userQuizId, userId, quiz);
                throw new WorkruitException("Quiz time limit exceeded");
            }
            UserQuizAnswers userQuizAnswersOld = userQuizAnswersRepository
                    .findByUserQuizIdAndUserIdAndQuestionId(userQuizId, userId, questionId);
            UserQuizAnswers userQuizAnswers = userQuizAnswersOld != null ? userQuizAnswersOld : new UserQuizAnswers();
            Question question = questionRepository.findById(questionId).get();
            if (question.getQuestionType() == QuestionType.INTEGER || question.getQuestionType() == QuestionType.TEXT
                    || question.getQuestionType() == QuestionType.SHORT_TEXT
                    || question.getQuestionType() == QuestionType.CHECKBOX
                    || question.getQuestionType() == QuestionType.DROPDOWN
                    || question.getQuestionType() == QuestionType.TRUE_OR_FALSE
                    || question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                    || question.getQuestionType() == QuestionType.AUDIO
                    || question.getQuestionType() == QuestionType.LIKE_OR_DISLIKE) {
                JsonNode node = objectMapper.readValue(objectMapper.writeValueAsString(object), JsonNode.class);
                String output = node.get("options").get(0).get("option").asText();
                if (StringUtils.isBlank(output) && userQuizAnswersOld != null) {
                    userQuizAnswersRepository.delete(userQuizAnswersOld);
                    analyticsService.updateUserQuizActions(userQuizId);
                    return null;
                }
                if (StringUtils.isBlank(output)) {
                    return null;
                }
            }
            userQuizAnswers.setOption(objectMapper.writeValueAsString(object));
            userQuizAnswers.setQuestionId(questionId);
            userQuizAnswers.setUserQuizId(userQuizId);
            userQuizAnswers.setUserId(userId);
            userQuizAnswers = userQuizAnswersRepository.save(userQuizAnswers);
            analyticsService.updateUserQuizActions(userQuizId);
            return userQuizAnswers.getId();
        } catch (Exception e) {
            log.error("Error while saving user answer quiz", e);
            throw e;
        }
    }

    public void handleQuizTimeComplete(Long userQuizId, Long userId, Quiz quiz) {
        List<Long> questionIds = questionRepository.findByQuizId(quiz.getId()).stream()
                .map(question -> question.getId()).collect(Collectors.toList());
        questionIds.stream().forEach(qId -> {
            UserQuizAnswers userQuizAnswer = userQuizAnswersRepository
                    .findByUserQuizIdAndUserIdAndQuestionId(userQuizId, userId, qId);
            if (userQuizAnswer == null) {
                try {
                    UserQuizAnswers uqa = new UserQuizAnswers();
                    uqa.setOption("");
                    uqa.setQuestionId(qId);
                    uqa.setUserQuizId(userQuizId);
                    uqa.setUserId(userId);
                    uqa = userQuizAnswersRepository.save(uqa);
                    analyticsService.updateUserQuizActions(userQuizId);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private Long checkAndQuizTimeLimits(UserQuiz userQuiz, Quiz quiz) throws WorkruitException {
        long startTime = userQuiz.getQuizStartTime().getTime();
        long totalTime = new Date().getTime() - startTime;

        String timeLimit = quiz.getQuizTimeLimit();
        if (StringUtils.isNotBlank(timeLimit) && !StringUtils.equalsIgnoreCase(quiz.getQuizTimeLimit(), "00:00:00")) {
            String[] limits = timeLimit.split(":");
            long hourTime = limits[0] != null && limits[0].length() > 0 && !StringUtils.equals(limits[0], "00")
                    ? (Long.parseLong(limits[0].charAt(0) == '0' ? String.valueOf(limits[0].charAt(1)) : limits[0]) * 60
                    * 60 * 1000)
                    : 0;
            long minTime = limits[1] != null && limits[1].length() > 0 && !StringUtils.equals(limits[1], "00")
                    ? (Long.parseLong(limits[1].charAt(0) == '0' ? String.valueOf(limits[1].charAt(1)) : limits[1]) * 60
                    * 1000)
                    : 0;
            long secTime = limits[2] != null && limits[2].length() > 0 && !StringUtils.equals(limits[2], "00")
                    ? (Long.parseLong(limits[2].charAt(0) == '0' ? String.valueOf(limits[2].charAt(1)) : limits[2])
                    * 1000)
                    : 0;
            log.debug("Time taken for quiz:{} , timeConsumed:{}, totalTime:{}", quiz.getId(),
                    (hourTime + minTime + secTime), totalTime);
            if ((hourTime + minTime + secTime) <= totalTime) {
                return null;
            } else {
                return ((hourTime + minTime + secTime) - totalTime) / 1000;
            }
        } else {
            return -1L;
        }
    }

    public UserQuizStatusDTO status(Long userId, Long userquizId) throws Exception {
        try {
            UserQuiz userQuiz = userQuizRepository.findByUserIdAndQuizId(userId, userquizId);
            if (userQuiz == null) {
                throw new WorkruitException("User Quiz not found");
            }
            String resultContent = userQuiz.getResultContent();
            UserQuizStatusDTO userQuizStatusDTO = new UserQuizStatusDTO();
            Long numberOfQuestions = questionRepository.countByQuizId(userQuiz.getQuizId());
            if (userQuiz.getStatus() == UserQuizStatus.COMPLETED
                    || userQuiz.getStatus() == UserQuizStatus.PENDING_REVIEW) {
                ObjectMapper objectMapper = new ObjectMapper();
                UserQuizStatusContent userQuizStatusContent = objectMapper.readValue(resultContent,
                        UserQuizStatusContent.class);
                userQuizStatusDTO.setCorrect(userQuizStatusContent.getCorrectAnswersCount());
                userQuizStatusDTO.setInCorrect(userQuizStatusContent.getInCorrectAnswersCount());
                userQuizStatusDTO.setInReview(userQuizStatusContent.getReviewAnswersCount());
                userQuizStatusDTO.setUnansweredCount(userQuizStatusContent.getUnansweredCount());
                userQuizStatusDTO.setLikeDislikeCount(userQuizStatusContent.getLikeDislikeCount());
                userQuizStatusDTO.setQuizSubmittedIn(userQuiz.getQuizSubmittedIn());
            } else if (userQuiz.getStatus() == UserQuizStatus.IN_PROGRESS) {
                Long userAnswersCount = userQuizAnswersRepository.countByUserQuizIdAndUserId(userquizId, userId);
                userQuizStatusDTO.setAnswered(userAnswersCount);
                userQuizStatusDTO.setSkipped(numberOfQuestions - userAnswersCount);
            }
            userQuizStatusDTO.setNumberOfQuestions(numberOfQuestions);
            return userQuizStatusDTO;
        } catch (Exception e) {
            throw e;
        }
    }

    public UserAnswerOptionDTO getUserQuizQuestionAnswer(Long userQuizId, Long userId, Long questionId)
            throws Exception {
        try {
            UserQuiz userQuiz = userQuizRepository.findByIdAndUserIdAndStatus(userQuizId, userId,
                    UserQuizStatus.COMPLETED);
            if (userQuiz == null) {
                throw new WorkruitException("Quiz is not completed yet");
            }
            Question question = questionRepository.findByIdAndQuizId(questionId, userQuiz.getQuizId());
            if (question == null) {
                throw new WorkruitException("Question doesnt belong to quiz");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            UserQuizStatusContent userQuizStatusContent = objectMapper.readValue(userQuiz.getResultContent(),
                    UserQuizStatusContent.class);
            Map<Long, Boolean> answerStatus = userQuizStatusContent.getAnswersStatus();
            Map<String, Object> questionData = objectMapper.readValue(question.getQuestion(), Map.class);
            Answer answer = answerRepository.findByQuestionId(questionId);
            UserQuizAnswers userQuizAnswers = userQuizAnswersRepository
                    .findByUserQuizIdAndUserIdAndQuestionId(userQuizId, userId, questionId);
            UserAnswerOptionDTO userAnswerOptionDTO = new UserAnswerOptionDTO();
            if (questionData != null) {
                questionData.entrySet().stream().forEach(entry -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if(key.equals("explanationImage") && value != null) {
                        try {
                             String encodeImage =  questionAnswerService.getImage(entry.getValue().toString());
                            userAnswerOptionDTO.getQuestion().put(entry.getKey(), encodeImage);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }else {
                        userAnswerOptionDTO.getQuestion().put(entry.getKey(), entry.getValue());
                    }
                });
            }
            JsonNode actualAnswer = objectMapper.readValue(answer.getOption(), JsonNode.class);
            if (userQuizAnswers == null || StringUtils.isBlank(userQuizAnswers.getOption())) {
                throw new WorkruitException("User did not answer this question");
            }
            JsonNode useranswer = objectMapper.readValue(userQuizAnswers.getOption(), JsonNode.class);
            if (question.getQuestionType() != QuestionType.SHORT_TEXT && question.getQuestionType() != QuestionType.TEXT
                    && question.getQuestionType() != QuestionType.INTEGER
                    && question.getQuestionType() != QuestionType.AUDIO) {
                Set<String> rightAnswers = new HashSet<>();
                ArrayNode rightArray = (ArrayNode) actualAnswer.get("options");
                for (int i = 0; i < rightArray.size(); i++) {
                    rightAnswers.add(rightArray.get(i).get("option").asText());
                }

                Set<String> userAnswerSet = new HashSet<>();
                ArrayNode userAnswerArray = (ArrayNode) useranswer.get("options");
                for (int i = 0; i < userAnswerArray.size(); i++) {
                    userAnswerSet.add(userAnswerArray.get(i).get("option").asText());
                }

                JsonNode jsonNode = objectMapper.readValue(question.getOptions(), JsonNode.class);
                ArrayNode optionsArray = (ArrayNode) jsonNode;
                for (int i = 0; i < optionsArray.size(); i++) {
                    String asText = optionsArray.get(i).get("option").asText();
                    ObjectNode node = (ObjectNode) optionsArray.get(i);
                    if (rightAnswers.contains(asText)) {
                        node.put("correctAnswer", true);
                    } else {
                        node.put("correctAnswer", false);
                    }
                    if (userAnswerSet.contains(asText)) {
                        node.put("selected", true);
                    } else {
                        node.put("selected", false);
                    }
                }
                userAnswerOptionDTO.setOptions(optionsArray);
            } else {
                ArrayNode options = objectMapper.readValue(question.getOptions(), ArrayNode.class);
                Map<String, Object> option = new HashMap<>();
                if (userQuizStatusContent.getAnswersStatus().get(questionId)) {
                    option.put("selected", true);
                } else {
                    option.put("selected", false);
                }
                option.put("correctAnswer", true);
                option.put("userAnswer", useranswer.get("options").get(0).get("option").asText());
                if (question.getQuestionType() == QuestionType.INTEGER
                        || question.getQuestionType() == QuestionType.TEXT
                        || question.getQuestionType() == QuestionType.SHORT_TEXT
                        || question.getQuestionType() == QuestionType.AUDIO) {
                    option.put("actualAnswer", actualAnswer.get("options").get(0).get("option").asText());
                }
                options.add(objectMapper.convertValue(option, JsonNode.class));
                userAnswerOptionDTO.setOptions(options);
            }
            userAnswerOptionDTO.setType(question.getQuestionType());
            return userAnswerOptionDTO;
        } catch (Exception e) {
            throw e;
        }
    }

    public List<UserQuizQuestionsResponse> getUserQuizQuestionsStatus(Long userQuizId, Long userId) throws Exception {
        try {
            Optional<UserQuiz> userQuizOptional = userQuizRepository.findById(userQuizId);
            if (!userQuizOptional.isPresent()) {
                throw new WorkruitException("Quiz does not exist with the mentioned Quiz Id");
            }
            UserQuiz userQuiz = userQuizOptional.get();
            if (userQuiz.getStatus() == UserQuiz.UserQuizStatus.COMPLETED) {
                ObjectMapper objectMapper = new ObjectMapper();
                UserQuizStatusContent userQuizStatusContent = objectMapper.readValue(userQuiz.getResultContent(),
                        UserQuizStatusContent.class);
                Map<Long, Boolean> answerStatus = userQuizStatusContent.getAnswersStatus();
                List<UserQuizQuestionsResponse> collect = answerStatus.entrySet().stream().map(entry -> {
                    UserQuizQuestionsResponse userQuizQuestionsResponse = new UserQuizQuestionsResponse();
                    try {
                        Long questionID = entry.getKey();
                        Question question = questionRepository.findById(questionID).get();
                        JsonNode jsonNode = objectMapper.readValue(question.getQuestion(), JsonNode.class);
                        String q = jsonNode.get("question").asText();
                        userQuizQuestionsResponse.setQuestion(q);
                        userQuizQuestionsResponse.setQuestionId(questionID);
                        if (entry.getValue() == null) {
                            if(question.getQuestionType()==QuestionType.LIKE_OR_DISLIKE){
                                UserQuizAnswers answer=userQuizAnswersRepository.findByUserQuizIdAndUserIdAndQuestionId(userQuizId,userId,questionID);
                                if(answer != null){
                                    JsonNode optionsJsonNode = objectMapper.readValue(answer.getOption(), JsonNode.class);
                                    JsonNode singleOption = objectMapper.readValue(optionsJsonNode.get("options").get(0).toString(), JsonNode.class);
                                    String optionStr = singleOption.get("option").asText();
                                    userQuizQuestionsResponse.setStatus((optionStr.equalsIgnoreCase(AnswerOption.LIKE.toString()) ? AnswerStatus.LIKE : AnswerStatus.DISLIKE));
                                } else {
                                    userQuizQuestionsResponse.setStatus(null);
                                }
                            }else {
                                userQuizQuestionsResponse.setStatus(AnswerStatus.INREVIEW);
                            }
                        } else if (entry.getValue()) {
                            userQuizQuestionsResponse.setStatus(AnswerStatus.CORRECT);
                        } else if (!entry.getValue()) {
                            userQuizQuestionsResponse.setStatus(AnswerStatus.INCORRECT);
                        }
                        userQuizQuestionsResponse.setQuestionCreatedDate(question.getCreatedDate());
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return userQuizQuestionsResponse;
                }).collect(Collectors.toList());
                Collections.sort(collect, new Comparator<UserQuizQuestionsResponse>() {

                    @Override
                    public int compare(UserQuizQuestionsResponse o1, UserQuizQuestionsResponse o2) {
                        if (o1 != null && o2 != null) {
                            if (o1.getQuestionCreatedDate().getTime() > o2.getQuestionCreatedDate().getTime()) {
                                return 1;
                            } else if (o1.getQuestionCreatedDate().getTime() < o2.getQuestionCreatedDate().getTime()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                        return 0;
                    }
                });
                List<UserQuizQuestionsResponse> sortedCollection = collect.stream().sorted(Comparator.comparing(UserQuizQuestionsResponse::getQuestionId))
                        .collect(Collectors.toList());
                return sortedCollection;
            } else {
                throw new WorkruitException("Quiz Id given is not Completed/In Review");
            }
        } catch (WorkruitException we) {
            throw we;
        } catch (Exception e) {
            throw e;
        }
    }

    public String getQuizTimeLeft(Long userId, Long quizId) throws WorkruitException {
        UserQuiz userQuiz = userQuizRepository.findByUserIdAndQuizId(userId, quizId);
        if (userQuiz != null && userQuiz.getStatus().name() == UserQuizStatus.IN_PROGRESS.name()) {
            Long checkAndQuizTimeLimits = checkAndQuizTimeLimits(userQuiz,
                    quizRepository.findById(userQuiz.getQuizId()).get());
            return parseQuizTimeLimits(checkAndQuizTimeLimits);
        }
        return "00:00:00";
    }

    private String parseQuizTimeLimits(Long checkAndQuizTimeLimits) {
        if (checkAndQuizTimeLimits != null && checkAndQuizTimeLimits > 0) {
            long hours = checkAndQuizTimeLimits / 3600;
            long secondsLeft = checkAndQuizTimeLimits - hours * 3600;
            long minutes = secondsLeft / 60;
            long seconds = secondsLeft - minutes * 60;
            String hoursOutput = String.valueOf(hours);
            String minutesOutput = String.valueOf(minutes);
            String secondsOutput = String.valueOf(seconds);
            String output = (hoursOutput.length() < 2 ? "0" + hoursOutput : hoursOutput) + ":"
                    + (minutesOutput.length() < 2 ? "0" + minutesOutput : minutesOutput) + ":"
                    + (secondsOutput.length() < 2 ? "0" + secondsOutput : secondsOutput);
            return output;
        } else {
            return "00:00:00";
        }
    }

    public static void main(String[] args) {

    }

    public void approveOrReject(Long userQuizId, Long questionId, boolean approve) throws Exception {
        try {
            Optional<UserQuiz> userQuizOptional = userQuizRepository.findById(userQuizId);
            if (!userQuizOptional.isPresent()) {
                throw new WorkruitException("User Quiz Not Found");
            }
            UserQuizAnswers userQuizAnswers = userQuizAnswersRepository
                    .findByUserQuizIdAndUserIdAndQuestionId(userQuizId, userQuizOptional.get().getUserId(), questionId);
            if (userQuizAnswers == null) {
                throw new WorkruitException("Question not found");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            UserQuizStatusContent userQuizStatusContent = objectMapper
                    .readValue(userQuizOptional.get().getResultContent(), UserQuizStatusContent.class);
            if (approve) {
                userQuizStatusContent.getAnswersStatus().put(questionId, Boolean.TRUE);
            } else {
                userQuizStatusContent.getAnswersStatus().put(questionId, Boolean.FALSE);
            }
            userQuizOptional.get().setResultContent(objectMapper.writeValueAsString(userQuizStatusContent));

            long total = userQuizStatusContent.getAnswersStatus().size() - userQuizStatusContent.getLikeDislikeCount();
            long actualTotal = userQuizStatusContent.getAnswersStatus().values().stream().filter(x -> x != null)
                    .count();
            long correctCount = userQuizStatusContent.getAnswersStatus().values().stream()
                    .filter(x -> x != null && x.booleanValue() == Boolean.TRUE.booleanValue()).count();
            long inCorrectCount = userQuizStatusContent.getAnswersStatus().values().stream()
                    .filter(x -> x != null && x.booleanValue() == Boolean.FALSE.booleanValue()).count();
            int percentage = (int) (correctCount * 100 / total);
            UserQuiz userQuiz = userQuizOptional.get();
            if (total == actualTotal) {
                userQuiz.setResult(UserQuizResult.PASSED);
                userQuiz.setStatus(UserQuizStatus.COMPLETED);
                userQuiz.setResultData(percentage);
            } else {
                userQuiz.setStatus(UserQuizStatus.PENDING_REVIEW);
            }
            userQuizStatusContent.setCorrectAnswersCount(correctCount);
            userQuizStatusContent.setInCorrectAnswersCount(inCorrectCount);
            userQuizStatusContent.setReviewAnswersCount(total - correctCount - inCorrectCount);
            userQuiz.setResultContent(objectMapper.writeValueAsString(userQuizStatusContent));
            userQuizRepository.save(userQuiz);
            analyticsService.updateUserQuizActions(userQuizId);
        } catch (WorkruitException we) {
            throw we;
        } catch (Exception e) {
            throw e;
        }
    }

    public String getTopicNamesForQuiz(Long quizId) {
        List<QuizTopicList> topics = quizTopicListRepository.findByQuizId(quizId);
        return Joiner.on(" ,")
                .join(topics.stream().map(topic -> topic.getTopicId())
                        .map(topicId -> categoryRepository.findById(topicId).get()).collect(Collectors.toList())
                        .stream().map(category -> category.getName()).collect(Collectors.toList()));

    }

    public String getCategoryNamesForQuiz(Long quizId) {
        List<QuizCategoryList> categories = quizCategoryListRepository.findByQuizId(quizId);
        return Joiner.on(" ,")
                .join(categories.stream().map(category -> category.getCategoryId())
                        .map(categoryId -> categoryRepository.findById(categoryId).get()).collect(Collectors.toList())
                        .stream().map(category -> category.getName()).collect(Collectors.toList()));

    }
}
