/**
 * 
 */
package com.workruit.quiz.services;

import com.workruit.quiz.persistence.entity.UserQuiz;
import com.workruit.quiz.persistence.entity.repository.UserQuizRepository;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.workruit.quiz.persistence.entity.repository.QuizRepository;

import java.util.Optional;

/**
 * @author Santosh Bhima
 *
 */
@Component
public class UserQuizTimeoutTask extends QuartzJobBean {

	private @Autowired QuizRepository quizRepository;
	private @Autowired UserQuizService userQuizService;
	private @Autowired UserQuizRepository userQuizRepository;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		Long userId = jobDataMap.getLong("userId");
		Long quizId = jobDataMap.getLong("quizId");
		Long userQuizId = jobDataMap.getLong("userQuizId");
		try {
			Optional<UserQuiz> userQuiz = userQuizRepository.findById(userQuizId);
			if(userQuiz.isPresent() && !userQuiz.get().getStatus().equals(UserQuiz.UserQuizStatus.COMPLETED) && userQuiz.get().getQuizSubmittedIn() == null) {
				userQuizService.handleQuizTimeComplete(userQuizId, userId, quizRepository.findById(quizId).get());
				userQuizService.submit(userQuizId, userId,true);
			}
		} catch (Exception e) {
			throw new JobExecutionException();
		}
	}

}
