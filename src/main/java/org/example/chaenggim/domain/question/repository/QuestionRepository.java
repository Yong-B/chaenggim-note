package org.example.chaenggim.domain.question.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.example.chaenggim.domain.question.entity.ProductTag;
import org.example.chaenggim.domain.question.entity.Question;
import org.example.chaenggim.domain.question.entity.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 모든 조회는 숨김 의료진의 질문을 제외한다(R7). 목록은 응답에 필요한 doctor·visit을 fetch join한다.
 * id 목록을 받는 집계 메서드에 빈 목록을 넘기지 않는다(서비스에서 비어 있으면 호출하지 않음).
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
            select q from Question q
            join fetch q.doctor d
            join fetch q.visit v
            where q.id = :id and d.deletedAt is null
            """)
    Optional<Question> findActiveById(@Param("id") Long id);

    // ---- 7.2 의료진 상세 ----

    /** 전달할 답변: READY, answeredAt ASC. */
    @Query("""
            select q from Question q
            join fetch q.doctor d
            join fetch q.visit v
            where d.id = :doctorId and d.deletedAt is null
              and q.status = org.example.chaenggim.domain.question.entity.QuestionStatus.READY
            order by q.answeredAt asc, q.id asc
            """)
    List<Question> findReadyByDoctorId(@Param("doctorId") Long doctorId);

    /** 확인이 필요한 질문: UNANSWERED·CHECKING, visit.visitDate ASC, id ASC. */
    @Query("""
            select q from Question q
            join fetch q.doctor d
            join fetch q.visit v
            where d.id = :doctorId and d.deletedAt is null
              and q.status in (org.example.chaenggim.domain.question.entity.QuestionStatus.UNANSWERED,
                               org.example.chaenggim.domain.question.entity.QuestionStatus.CHECKING)
            order by v.visitDate asc, q.id asc
            """)
    List<Question> findOpenByDoctorId(@Param("doctorId") Long doctorId);

    /** 전달 완료한 질문: DELIVERED, deliveredAt DESC. */
    @Query("""
            select q from Question q
            join fetch q.doctor d
            join fetch q.visit v
            where d.id = :doctorId and d.deletedAt is null
              and q.status = org.example.chaenggim.domain.question.entity.QuestionStatus.DELIVERED
            order by q.deliveredAt desc, q.id desc
            """)
    List<Question> findDeliveredByDoctorId(@Param("doctorId") Long doctorId);

    // ---- 7.3 질문함 (product가 null이면 제품 필터 없음, P1) ----

    /** DELIVERED 외 상태: visit.visitDate ASC, id ASC. */
    @Query("""
            select q from Question q
            join fetch q.doctor d
            join fetch q.visit v
            where d.deletedAt is null and q.status = :status
              and (:product is null or q.productTag = :product)
            order by v.visitDate asc, q.id asc
            """)
    List<Question> findActiveByStatusOrderByVisitDate(@Param("status") QuestionStatus status,
                                                      @Param("product") ProductTag product);

    /** DELIVERED: deliveredAt DESC. */
    @Query("""
            select q from Question q
            join fetch q.doctor d
            join fetch q.visit v
            where d.deletedAt is null
              and q.status = org.example.chaenggim.domain.question.entity.QuestionStatus.DELIVERED
              and (:product is null or q.productTag = :product)
            order by q.deliveredAt desc, q.id desc
            """)
    List<Question> findActiveDelivered(@Param("product") ProductTag product);

    // ---- 집계 (GROUP BY) ----

    /** 상태별 개수(product가 null이면 전체). 개수가 0인 상태는 결과에 없다. */
    @Query("""
            select new org.example.chaenggim.domain.question.repository.StatusCount(q.status, count(q))
            from Question q
            join q.doctor d
            where d.deletedAt is null
              and (:product is null or q.productTag = :product)
            group by q.status
            """)
    List<StatusCount> countActiveByStatus(@Param("product") ProductTag product);

    @Query("""
            select new org.example.chaenggim.domain.question.repository.DoctorReadyCount(d.id, count(q))
            from Question q
            join q.doctor d
            where d.id in :doctorIds and d.deletedAt is null
              and q.status = org.example.chaenggim.domain.question.entity.QuestionStatus.READY
            group by d.id
            """)
    List<DoctorReadyCount> countReadyByDoctorIds(@Param("doctorIds") Collection<Long> doctorIds);

    @Query("""
            select new org.example.chaenggim.domain.question.repository.VisitQuestionCount(
                q.visit.id,
                count(q),
                sum(case when q.status = org.example.chaenggim.domain.question.entity.QuestionStatus.DELIVERED
                         then 1L else 0L end))
            from Question q
            join q.doctor d
            where q.visit.id in :visitIds and d.deletedAt is null
            group by q.visit.id
            """)
    List<VisitQuestionCount> countByVisitIds(@Param("visitIds") Collection<Long> visitIds);

    // ---- 방문 삭제(D16) ----

    boolean existsByVisit_IdAndStatus(Long visitId, QuestionStatus status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Question q where q.visit.id = :visitId")
    int deleteByVisitId(@Param("visitId") Long visitId);
}
