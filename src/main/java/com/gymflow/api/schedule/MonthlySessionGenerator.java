package com.gymflow.api.schedule;

import com.gymflow.api.repository.ClassSessionRepository;
import com.gymflow.api.repository.GymflowClassRepository;
import com.gymflow.api.repository.entity.ClassSessionEntity;
import com.gymflow.api.repository.entity.GymflowClassEntity;
import jakarta.annotation.PostConstruct;
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

    // -------------------------------
    // 1) RUN MONTHLY: 15th @ 03:00
    // -------------------------------
    @Scheduled(cron = "0 0 3 15 * *")
    public void generateNextMonthSessions() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        generateForMonth(nextMonth);
    }

    // -------------------------------
    // 2) RUN ON STARTUP
    // -------------------------------
    @PostConstruct
    public void initOnStartup() {
        YearMonth current = YearMonth.now();
        YearMonth next = YearMonth.now().plusMonths(1);

        generateForMonth(current);
        generateForMonth(next);
    }

    // -------------------------------
    // REUSABLE LOGIC
    // -------------------------------
    private void generateForMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        var classes = classRepo.findAll();

        for (GymflowClassEntity cls : classes) {
            generateSessionsForRange(cls, start, end);
        }
    }

    private void generateSessionsForRange(GymflowClassEntity cls, LocalDate start, LocalDate end) {
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {

            if (!cls.getDays().contains(day.getDayOfWeek())) {
                continue;
            }

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
