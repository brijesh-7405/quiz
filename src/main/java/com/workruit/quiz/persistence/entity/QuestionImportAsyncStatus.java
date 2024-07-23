package com.workruit.quiz.persistence.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "question_import_status")
public class QuestionImportAsyncStatus extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "question_import_status_id")
    private Long questionImportAsyncStatusId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private QuestionImportJobStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "version")
    private Long version;
    public static enum QuestionImportJobStatus {
        CREATED, INPROGRESS, FAILED, SUCCESS
    }

}
