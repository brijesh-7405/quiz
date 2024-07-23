/**
 * 
 */
package com.workruit.quiz.security;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.workruit.quiz.configuration.WorkruitAuthorizationException;
import com.workruit.quiz.controllers.dto.UserDetailsDTO;

/**
 * @author Santosh
 *
 */
@Aspect
@Component
public class UserAuthorizedAspect {
	@Before("@annotation(com.workruit.quiz.security.UserAuthorized)")
	public void before(JoinPoint joinpoint) throws Exception {
		MethodInvocationProceedingJoinPoint methodInvocationProceedingJoinPoint = (MethodInvocationProceedingJoinPoint) joinpoint;
		MethodSignature signature = (MethodSignature) methodInvocationProceedingJoinPoint.getSignature();
		Method method = signature.getMethod();
		UserAuthorized userAuthorized = AnnotationUtils.findAnnotation(method, UserAuthorized.class);
		UserDetailsDTO userDetailsDTO = (UserDetailsDTO) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		if (userAuthorized != null) {
			if (userDetailsDTO.getUserRole().equals(userAuthorized.userRoles()[0].name())
					|| (userAuthorized.userRoles().length > 1
							&& userDetailsDTO.getUserRole().equals(userAuthorized.userRoles()[1].name()))) {

			} else {
				throw new WorkruitAuthorizationException("");
			}
		}
	}
}
