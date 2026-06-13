package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.component.NextScheduledAtCalculationStrategy;
import org.mini_lab.personal_cloud_sync.dto.NextScheduledAtRequest;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NextScheduledAtCalculationStrategyTest {

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-06-05T10:00:00Z"),
            ZoneOffset.UTC
    );

    @Test
    void if_schedule_type_is_manual_return_empty() {
        NextScheduledAtRequest nextScheduledAtRequest = NextScheduledAtRequest.builder()
                .scheduleType(ScheduleType.MANUAL)
                .runTime(null)
                .scheduleInterval(null)
                .build();
        assertEquals(Optional.empty(), NextScheduledAtCalculationStrategy.estimateNextScheduledAt(nextScheduledAtRequest, fixedClock));
    }

    @Test
    void estimateNextScheduledAt_interval_shouldReturnFixedNowPlusScheduleIntervalHours() {
        // given

        NextScheduledAtRequest request = new NextScheduledAtRequest();
        request.setScheduleType(ScheduleType.INTERVAL);
        request.setScheduleInterval((short) 6);
        request.setRunTime(null);

        // when
        Optional<OffsetDateTime> result =
                NextScheduledAtCalculationStrategy.estimateNextScheduledAt(request, fixedClock);

        // then
        assertTrue(result.isPresent());

        OffsetDateTime expected = OffsetDateTime.parse("2026-06-05T16:00:00Z");

        assertEquals(expected, result.get());
    }

    @Test
    void estimateNextScheduledAt_daily_shouldReturnTomorrow_whenRuntimeAlreadyPassed() {
        NextScheduledAtRequest request = new NextScheduledAtRequest();
        request.setScheduleType(ScheduleType.DAILY);
        request.setRunTime(LocalTime.parse("06:00"));

        assertEquals(OffsetDateTime.parse("2026-06-06T06:00:00Z"), NextScheduledAtCalculationStrategy.estimateNextScheduledAt(request, fixedClock).orElseThrow());

    }

    @Test
    void estimateNextScheduledAt_daily_shouldReturnTodayRuntime_whenRuntimeNotPassed() {
        NextScheduledAtRequest request = new NextScheduledAtRequest();
        request.setScheduleType(ScheduleType.DAILY);
        request.setRunTime(LocalTime.parse("10:00"));

        assertEquals(OffsetDateTime.parse("2026-06-06T10:00:00Z"), NextScheduledAtCalculationStrategy.estimateNextScheduledAt(request, fixedClock).orElseThrow());
    }

}