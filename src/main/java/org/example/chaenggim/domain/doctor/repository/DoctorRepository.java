package org.example.chaenggim.domain.doctor.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 모든 조회는 숨김 의료진(deletedAt not null)을 제외한다(R7). 예외: JpaRepository 기본 count()는 숨김 포함(시드 판단용). */
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query("select d from Doctor d where d.id = :id and d.deletedAt is null")
    Optional<Doctor> findActiveById(@Param("id") Long id);

    /** 7.4 의료진 검색. keyword가 null이면 전체. 이름 또는 병원에 포함(대소문자 무시), 진료과는 대상 아님. */
    @Query("""
            select d from Doctor d
            where d.deletedAt is null
              and (:keyword is null
                   or lower(d.name) like lower(concat('%', :keyword, '%'))
                   or lower(d.hospital) like lower(concat('%', :keyword, '%')))
            order by d.name asc, d.id asc
            """)
    List<Doctor> searchActive(@Param("keyword") String keyword);

    /** 7.1 이번 주 방문 예정: nextVisitDate <= until (지난 예정일 포함). */
    @Query("""
            select d from Doctor d
            where d.deletedAt is null
              and d.nextVisitDate is not null
              and d.nextVisitDate <= :until
            order by d.nextVisitDate asc, d.name asc
            """)
    List<Doctor> findActiveUpcoming(@Param("until") LocalDate until);
}
