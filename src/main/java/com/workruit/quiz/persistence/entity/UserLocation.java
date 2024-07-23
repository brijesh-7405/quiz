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

import lombok.Data;

/**
 * @author Santosh
 *
 */
@Table(name = "user_location")
@Entity
@Data
public class UserLocation extends BaseEntity {

	public UserLocation() {
		super();
	}

	public UserLocation(Long userId, String city, String state, String country, String address) {
		super();
		this.userId = userId;
		this.city = city;
		this.state = state;
		this.country = country;
		this.address = address;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "city")
	private String city;

	@Column(name = "state")
	private String state;

	@Column(name = "country")
	private String country;

	@Column(name = "address")
	private String address;
}
