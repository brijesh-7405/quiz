/**
 * 
 */
package com.workruit.quiz.controllers.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh
 *
 */
@Getter
@Setter
public class CategoryDTO {
	@Null
	private Long id;
	@NotNull
	@Size(min = 1, max = 25)
	private String name;
	@NotNull
	@JsonInclude(value = Include.NON_NULL)
	private String description;
	@JsonInclude(value = Include.NON_NULL)
	private Long parentId;
	@JsonInclude(value = Include.NON_NULL)
	private String logoURL;

	@Builder
	public CategoryDTO(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public CategoryDTO() {
		super();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CategoryDTO other = (CategoryDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public CategoryDTO(@Null Long id) {
		super();
		this.id = id;
	}

}
