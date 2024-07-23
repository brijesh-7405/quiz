package com.workruit.quiz.constants;

public enum AnswerOption {
    LIKE("yes"), DISLIKE("no");

    private final String status;

    AnswerOption(String status) {
        this.status = status;
    }
    public String toString() {
        return status;
    }
}
