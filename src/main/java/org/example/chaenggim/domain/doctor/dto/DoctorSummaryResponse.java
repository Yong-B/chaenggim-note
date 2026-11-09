package org.example.chaenggim.domain.doctor.dto;

import java.time.LocalDate;
import org.example.chaenggim.domain.doctor.entity.Doctor;

public record DoctorSummaryResponse(
        Long id,
        String name,
        String hospital,
        String department,
        LocalDate nextVisitDate
) {

    public static DoctorSummaryResponse from(Doctor doctor) {
        return new DoctorSummaryResponse(doctor.getId(), doctor.getName(), doctor.getHospital(),
                doctor.getDepartment(), doctor.getNextVisitDate());
    }
}
