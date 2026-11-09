package org.example.chaenggim.domain.question.entity;

import lombok.Getter;

@Getter
public enum SourceType {
    LABEL("허가사항"),
    CLINICAL("임상자료"),
    MEDICAL_TEAM("학술팀 회신");

    private final String label;

    SourceType(String label) {
        this.label = label;
    }
}
