package org.example.chaenggim.domain.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.global.util.Texts;

/** 의료진 등록·수정 공용. 수정 시 nextVisitDate가 null이면 예정일을 지운다. */
public record DoctorRequest(
        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 30, message = "이름은 30자 이하로 입력해 주세요.")
        String name,

        @NotBlank(message = "병원을 입력해 주세요.")
        @Size(max = 50, message = "병원은 50자 이하로 입력해 주세요.")
        String hospital,

        @NotBlank(message = "진료과를 입력해 주세요.")
        @Size(max = 30, message = "진료과는 30자 이하로 입력해 주세요.")
        String department,

        @Size(max = 500, message = "메모는 500자 이하로 입력해 주세요.")
        String memo,

        LocalDate nextVisitDate
) {

    public DoctorRequest {
        name = Texts.strip(name);
        hospital = Texts.strip(hospital);
        department = Texts.strip(department);
        memo = Texts.stripToNull(memo);
    }

    public Doctor toEntity() {
        return Doctor.builder()
                .name(name)
                .hospital(hospital)
                .department(department)
                .memo(memo)
                .nextVisitDate(nextVisitDate)
                .build();
    }
}
