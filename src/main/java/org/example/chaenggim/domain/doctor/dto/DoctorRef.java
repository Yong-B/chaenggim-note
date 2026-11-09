package org.example.chaenggim.domain.doctor.dto;

import org.example.chaenggim.domain.doctor.entity.Doctor;

/** 다른 응답 안에서 의료진을 가리킬 때 (05 API 명세 2절). */
public record DoctorRef(Long id, String name, String hospital) {

    public static DoctorRef from(Doctor doctor) {
        return new DoctorRef(doctor.getId(), doctor.getName(), doctor.getHospital());
    }
}
