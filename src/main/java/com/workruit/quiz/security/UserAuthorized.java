/**
 * 
 */
package com.workruit.quiz.security;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.workruit.quiz.persistence.entity.User.UserRole;

@Retention(RUNTIME)
@Target(METHOD)
public @interface UserAuthorized {
	UserRole[] userRoles() default { UserRole.DEFAULT_USER };
}
