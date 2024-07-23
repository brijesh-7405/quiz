package com.workruit.quiz.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class AddSocialPhoneDTO {

    @Pattern(regexp = "^\\d{10}$", message = "Invalid Phone number")
    private String phoneNumber;
}
