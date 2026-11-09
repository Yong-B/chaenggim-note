package org.example.chaenggim.domain.question.entity;

import lombok.Getter;

/** P1 제품 태그. 질문 작성 시 선택, 질문함 제품 필터에 쓴다. */
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
