package org.example.chaenggim.domain.commitment.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.commitment.dto.CommitmentResponse;
import org.example.chaenggim.domain.commitment.dto.CommitmentUpdateRequest;
import org.example.chaenggim.domain.commitment.entity.Commitment;
import org.example.chaenggim.domain.commitment.repository.CommitmentRepository;
import org.example.chaenggim.global.error.BusinessException;
import org.example.chaenggim.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommitmentService {

    private final Clock clock;
    private final CommitmentRepository commitmentRepository;

    public CommitmentResponse get(Long id) {
        return CommitmentResponse.from(findActive(id), today());
    }

    /**
     * R4: 완료된 약속은 수정 불가(COMMITMENT_DONE, 먼저 검사).
     * R5: 기한을 바꿀 때만 오늘 이전 불가. 기한을 그대로 두면 지난 기한이어도 내용 수정 허용.
     */
    @Transactional
    public CommitmentResponse update(Long id, CommitmentUpdateRequest request) {
        Commitment commitment = findActive(id);
        LocalDate today = today();
        if (commitment.isDone()) {
            throw new BusinessException(ErrorCode.COMMITMENT_DONE);
        }
        boolean dueDateChanged = !request.dueDate().isEqual(commitment.getDueDate());
        if (dueDateChanged && request.dueDate().isBefore(today)) {
            throw BusinessException.fieldError("dueDate", "약속 기한은 오늘 이전일 수 없습니다.");
        }
        commitment.update(request.content(), request.dueDate());
        return CommitmentResponse.from(commitment, today);
    }

    @Transactional
    public CommitmentResponse complete(Long id) {
        Commitment commitment = findActive(id);
        commitment.complete(LocalDateTime.now(clock));
        return CommitmentResponse.from(commitment, today());
    }

    @Transactional
    public CommitmentResponse reopen(Long id) {
        Commitment commitment = findActive(id);
        commitment.reopen();
        return CommitmentResponse.from(commitment, today());
    }

    /** R6: 완료 여부와 관계없이 삭제 가능. */
    @Transactional
    public void delete(Long id) {
        commitmentRepository.delete(findActive(id));
    }

    private Commitment findActive(Long id) {
        return commitmentRepository.findActiveById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
