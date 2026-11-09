package com.chaenggim.question.domain;

import lombok.Getter;

/** P1. T15 전까지 Question.productTag는 항상 null. */
@Getter
public enum ProductTag {
    ROSUZET("로수젯"),
    AMOSARTAN("아모잘탄"),
    AMOPREL("아모프렐"),
    EFE("에페"),
    ROLONTIS("롤론티스");

    private final String label;

    ProductTag(String label) {
        this.label = label;
    }
}
