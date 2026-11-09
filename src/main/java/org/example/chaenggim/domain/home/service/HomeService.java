package org.example.chaenggim.domain.home.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.chaenggim.domain.commitment.dto.CommitmentResponse;
import org.example.chaenggim.domain.commitment.repository.CommitmentRepository;
import org.example.chaenggim.domain.commitment.repository.DoctorOpenCommitmentCount;
import org.example.chaenggim.domain.doctor.entity.Doctor;
import org.example.chaenggim.domain.doctor.repository.DoctorRepository;
import org.example.chaenggim.domain.home.dto.HomeResponse;
import org.example.chaenggim.domain.home.dto.HomeResponse.UpcomingDoctor;
import org.example.chaenggim.domain.question.entity.QuestionStatus;
import org.example.chaenggim.domain.question.repository.DoctorReadyCount;
import org.example.chaenggim.domain.question.repository.QuestionRepository;
import org.example.chaenggim.domain.question.repository.StatusCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 7.1 홈 "오늘 챙길 것". 의료진별 숫자는 GROUP BY 집계 2번으로 가져와 합친다(N+1 없음). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final List<QuestionStatus> HOME_STATUSES =
            List.of(QuestionStatus.UNANSWERED, QuestionStatus.CHECKING, QuestionStatus.READY);

    private final Clock clock;
    private final DoctorRepository doctorRepository;
    private final QuestionRepository questionRepository;
    private final CommitmentRepository commitmentRepository;

    public HomeResponse load() {
        LocalDate today = LocalDate.now(clock);

        List<CommitmentResponse> overdue = commitmentRepository.findActiveOverdue(today).stream()
                .map(c -> CommitmentResponse.from(c, today)).toList();
        List<CommitmentResponse> dueToday = commitmentRepository.findActiveDueToday(today).stream()
                .map(c -> CommitmentResponse.from(c, today)).toList();

        return new HomeResponse(today, overdue, dueToday, upcomingDoctors(today), questionCounts());
    }

    private List<UpcomingDoctor> upcomingDoctors(LocalDate today) {
        List<Doctor> doctors = doctorRepository.findActiveUpcoming(today.plusDays(6));
        if (doctors.isEmpty()) {
            return List.of();
        }
        List<Long> ids = doctors.stream().map(Doctor::getId).toList();
        Map<Long, Long> readyCounts = questionRepository.countReadyByDoctorIds(ids).stream()
                .collect(Collectors.toMap(DoctorReadyCount::doctorId, DoctorReadyCount::count));
        Map<Long, Long> openCounts = commitmentRepository.countOpenByDoctorIds(ids).stream()
                .collect(Collectors.toMap(DoctorOpenCommitmentCount::doctorId, DoctorOpenCommitmentCount::count));

        return doctors.stream()
                .map(d -> UpcomingDoctor.of(d, today,
                        readyCounts.getOrDefault(d.getId(), 0L),
                        openCounts.getOrDefault(d.getId(), 0L)))
                .toList();
    }

    private Map<QuestionStatus, Long> questionCounts() {
        Map<QuestionStatus, Long> counts = new EnumMap<>(QuestionStatus.class);
        HOME_STATUSES.forEach(s -> counts.put(s, 0L));
        for (StatusCount c : questionRepository.countActiveByStatus()) {
            if (counts.containsKey(c.status())) {
                counts.put(c.status(), c.count());
            }
        }
        return counts;
    }
}
