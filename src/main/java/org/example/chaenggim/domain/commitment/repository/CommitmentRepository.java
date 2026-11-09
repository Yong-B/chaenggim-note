package org.example.chaenggim.domain.commitment.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.example.chaenggim.domain.commitment.entity.Commitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 모든 조회는 숨김 의료진의 약속을 제외한다(R7). 목록은 응답에 필요한 doctor를 fetch join한다.
 * id 목록을 받는 집계 메서드에 빈 목록을 넘기지 않는다(서비스에서 비어 있으면 호출하지 않음).
 */
public interface CommitmentRepository extends JpaRepository<Commitment, Long> {

    @Query("""
            select c from Commitment c
            join fetch c.doctor d
            join fetch c.visit v
            where c.id = :id and d.deletedAt is null
            """)
    Optional<Commitment> findActiveById(@Param("id") Long id);

    // ---- 7.1 홈 ----

    /** 기한 지난 약속: done = false AND dueDate < today, dueDate ASC, id ASC. */
    @Query("""
            select c from Commitment c
            join fetch c.doctor d
            where d.deletedAt is null and c.done = false and c.dueDate < :today
            order by c.dueDate asc, c.id asc
            """)
    List<Commitment> findActiveOverdue(@Param("today") LocalDate today);

    /** 오늘까지인 약속: done = false AND dueDate = today, id ASC. */
    @Query("""
            select c from Commitment c
            join fetch c.doctor d
            where d.deletedAt is null and c.done = false and c.dueDate = :today
            order by c.id asc
            """)
    List<Commitment> findActiveDueToday(@Param("today") LocalDate today);

    // ---- 7.2 의료진 상세 ----

    /** 남은 약속: done = false, dueDate ASC, id ASC. */
    @Query("""
            select c from Commitment c
            join fetch c.doctor d
            where d.id = :doctorId and d.deletedAt is null and c.done = false
            order by c.dueDate asc, c.id asc
            """)
    List<Commitment> findOpenByDoctorId(@Param("doctorId") Long doctorId);

    /** 완료한 약속: done = true, doneAt DESC. */
    @Query("""
            select c from Commitment c
            join fetch c.doctor d
            where d.id = :doctorId and d.deletedAt is null and c.done = true
            order by c.doneAt desc, c.id desc
            """)
    List<Commitment> findDoneByDoctorId(@Param("doctorId") Long doctorId);

    // ---- 집계 (GROUP BY) ----

    @Query("""
            select new org.example.chaenggim.domain.commitment.repository.DoctorOpenCommitmentCount(d.id, count(c))
            from Commitment c
            join c.doctor d
            where d.id in :doctorIds and d.deletedAt is null and c.done = false
            group by d.id
            """)
    List<DoctorOpenCommitmentCount> countOpenByDoctorIds(@Param("doctorIds") Collection<Long> doctorIds);

    @Query("""
            select new org.example.chaenggim.domain.commitment.repository.VisitCommitmentCount(c.visit.id, count(c))
            from Commitment c
            join c.doctor d
            where c.visit.id in :visitIds and d.deletedAt is null
            group by c.visit.id
            """)
    List<VisitCommitmentCount> countByVisitIds(@Param("visitIds") Collection<Long> visitIds);

    // ---- 방문 삭제(D16) ----

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Commitment c where c.visit.id = :visitId")
    int deleteByVisitId(@Param("visitId") Long visitId);
}
