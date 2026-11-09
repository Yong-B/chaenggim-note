package org.example.chaenggim.domain.doctor.dto;

import java.time.LocalDate;
import org.example.chaenggim.domain.doctor.entity.Doctor;

public record DoctorResponse(
        Long id,
        String name,
        String hospital,
        String department,
        String memo,
        LocalDate nextVisitDate
) {

    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(doctor.getId(), doctor.getName(), doctor.getHospital(),
                doctor.getDepartment(), doctor.getMemo(), doctor.getNextVisitDate());
    }
}
