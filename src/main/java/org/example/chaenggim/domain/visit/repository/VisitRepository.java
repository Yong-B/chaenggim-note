package org.example.chaenggim.domain.visit.repository;

import java.util.List;
import java.util.Optional;
import org.example.chaenggim.domain.visit.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 모든 조회는 숨김 의료진의 방문을 제외한다(R7). */
public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("""
            select v from Visit v
            join fetch v.doctor d
            where v.id = :id and d.deletedAt is null
            """)
    Optional<Visit> findActiveById(@Param("id") Long id);

    /** 7.2 방문 기록 / 최근 방문(첫 번째 항목). */
    @Query("""
            select v from Visit v
            join v.doctor d
            where d.id = :doctorId and d.deletedAt is null
            order by v.visitDate desc, v.id desc
            """)
    List<Visit> findActiveByDoctorId(@Param("doctorId") Long doctorId);
}
