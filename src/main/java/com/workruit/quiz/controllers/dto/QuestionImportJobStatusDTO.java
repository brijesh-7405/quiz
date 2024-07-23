package com.workruit.quiz.controllers.dto;

import com.workruit.quiz.persistence.entity.QuestionImportAsyncStatus;
import lombok.Data;

@Data
public class QuestionImportJobStatusDTO {
    private QuestionImportAsyncStatus.QuestionImportJobStatus status;
    private String description;
}
