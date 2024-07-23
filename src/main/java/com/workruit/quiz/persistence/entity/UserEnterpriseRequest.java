/**
 * 
 */
package com.workruit.quiz.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Santosh Bhima
 *
 */
@Entity
@Table(name = "user_enterprise_request")
public class UserEnterpriseRequest extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private long id;

	@Column(name = "user_id")
	private long userId;

	@Column(name = "enterprise_id")
	private long enterpriseId;

	@Column(name = "approve_status")
	private boolean approveStatus = false;

	@Column(name = "user_rejected")
	private boolean userRejected = false;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the userId
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(long userId) {
		this.userId = userId;
	}

	/**
	 * @return the enterpriseId
	 */
	public long getEnterpriseId() {
		return enterpriseId;
	}

	/**
	 * @param enterpriseId the enterpriseId to set
	 */
	public void setEnterpriseId(long enterpriseId) {
		this.enterpriseId = enterpriseId;
	}

	/**
	 * @return the approveStatus
	 */
	public boolean isApproveStatus() {
		return approveStatus;
	}

	/**
	 * @param approveStatus the approveStatus to set
	 */
	public void setApproveStatus(boolean approveStatus) {
		this.approveStatus = approveStatus;
	}

	/**
	 * @return the userRejected
	 */
	public boolean isUserRejected() {
		return userRejected;
	}

	/**
	 * @param userRejected the userRejected to set
	 */
	public void setUserRejected(boolean userRejected) {
		this.userRejected = userRejected;
	}

}
