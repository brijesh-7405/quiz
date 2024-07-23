package com.workruit.quiz.persistence.entity.repository;

import com.workruit.quiz.persistence.entity.QuestionImportAsyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionImportAsyncStatusRepository extends JpaRepository<QuestionImportAsyncStatus, Long> {
    QuestionImportAsyncStatus findByQuestionImportAsyncStatusIdAndUserId(Long id, Long userId);

    @Query(value = "SELECT max(version) FROM question_import_status where user_id=?1",nativeQuery = true)
    Long findVersionByQuestionImportAsyncUserId(Long userId);
}
