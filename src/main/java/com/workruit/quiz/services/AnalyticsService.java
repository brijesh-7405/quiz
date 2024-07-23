package com.workruit.quiz.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.workruit.quiz.persistence.entity.Question;
import com.workruit.quiz.persistence.entity.UserQuiz;
import com.workruit.quiz.persistence.entity.repository.QuestionRepository;
import com.workruit.quiz.persistence.entity.repository.UserQuizRepository;

@Component
public class AnalyticsService {
	private @Autowired QuizAnalyticsService quizAnalyticsService;
	private @Autowired UserQuizAnalyticsService userQuizAnalyticsService;
	private @Autowired QuestionAnalyticsService questionAnalyticsService;
	private @Autowired QuestionRepository questionRepository;
	private @Autowired UserQuizRepository userQuizRepository;

	public void updateQuestionActions(Long questionId) throws Exception {
		Question question = questionRepository.findById(questionId).get();
		questionAnalyticsService.persistOne(question.getQuizId());
		quizAnalyticsService.persistOne(question.getQuizId());
		userQuizRepository.findByQuizId(question.getQuizId()).stream()
				.forEach(userQuiz -> userQuizAnalyticsService.persistOne(userQuiz.getId()));
	}

	public void updateQuizActions(Long quizId) throws Exception {
		questionAnalyticsService.persistOne(quizId);
		quizAnalyticsService.persistOne(quizId);
	}

	public void updateUserQuizActions(Long userQuizId) throws Exception {
		userQuizAnalyticsService.persistOne(userQuizId);
		UserQuiz userQuiz = userQuizRepository.findById(userQuizId).get();
		questionAnalyticsService.persistOne(userQuiz.getQuizId());
		quizAnalyticsService.persistOne(userQuiz.getQuizId());
	}
}
