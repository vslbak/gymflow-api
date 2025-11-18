package com.gymflow.api.schedule;

import com.gymflow.api.repository.ClassSessionRepository;
import com.gymflow.api.repository.GymflowClassRepository;
import com.gymflow.api.repository.entity.ClassSessionEntity;
import com.gymflow.api.repository.entity.GymflowClassEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class MonthlySessionGenerator {

    private final GymflowClassRepository classRepo;
    private final ClassSessionRepository sessionRepo;

    // 03:00 on the 15th of every month
    @Scheduled(cron = "0 0 3 15 * *")
    public void generateNextMonthSessions() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        LocalDate start = nextMonth.atDay(1);
        LocalDate end = nextMonth.atEndOfMonth();

        var classes = classRepo.findAll();

        for (GymflowClassEntity cls : classes) {
            generateSessionsForRange(cls, start, end);
        }
    }

    private void generateSessionsForRange(GymflowClassEntity cls, LocalDate start, LocalDate end) {
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            // only generate on configured days
            if (!cls.getDays().contains(day.getDayOfWeek())) {
                continue;
            }

            // avoid duplicates
            boolean exists = sessionRepo.existsByGymflowClassIdAndDate(cls.getId(), day);
            if (exists) {
                continue;
            }

            ClassSessionEntity session = new ClassSessionEntity();
            session.setGymflowClass(new GymflowClassEntity(cls.getId()));
            session.setDate(day);
            session.setSpotsLeft(cls.getTotalSpots());

            sessionRepo.save(session);
        }
    }
}
