package org.mini_lab.personal_cloud_sync.services;

import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.dto.NextScheduledAtRequest;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NextScheduledAtCalculationStrategyTest {

    @Test
    void if_schedule_type_is_manual_return_empty() {
        NextScheduledAtRequest nextScheduledAtRequest = NextScheduledAtRequest.builder()
                .scheduleType(null)
                .runTime(null)
                .scheduleInterval(null)
                .build();
        assertEquals(Optional.empty(), NextScheduledAtCalculationStrategy.estimateNextScheduledAt(nextScheduledAtRequest));
    }

    @Test
    void estimateNextScheduledAt_interval_shouldReturnFixedNowPlusScheduleIntervalHours() {
        // given
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-05T10:00:00Z"),
                ZoneOffset.UTC
        );

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
    void estimateNextScheduledAt_interval_shouldThrowException_whenRunTimeExists() {
        // given
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-05T10:00:00Z"),
                ZoneOffset.UTC
        );

        NextScheduledAtRequest request = new NextScheduledAtRequest();
        request.setScheduleType(ScheduleType.INTERVAL);
        request.setScheduleInterval((short) 6);
        request.setRunTime(LocalTime.of(8, 0));

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> NextScheduledAtCalculationStrategy.estimateNextScheduledAt(request, fixedClock)
        );

        assertEquals("INTERVAL schedule must not have runTime", exception.getMessage());
    }

    @Test
    void estimateNextScheduledAt_interval_shouldThrowException_whenScheduleIntervalIsNull() {
        // given
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-05T10:00:00Z"),
                ZoneOffset.UTC
        );

        NextScheduledAtRequest request = new NextScheduledAtRequest();
        request.setScheduleType(ScheduleType.INTERVAL);
        request.setScheduleInterval(null);
        request.setRunTime(null);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> NextScheduledAtCalculationStrategy.estimateNextScheduledAt(request, fixedClock)
        );

        assertEquals("INTERVAL schedule requires scheduleInterval", exception.getMessage());
    }

    @Test
    void estimateNextScheduledAt_interval_shouldThrowException_whenScheduleIntervalIsLessThanOrEqualZero() {
        // given
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-05T10:00:00Z"),
                ZoneOffset.UTC
        );

        NextScheduledAtRequest request = new NextScheduledAtRequest();
        request.setScheduleType(ScheduleType.INTERVAL);
        request.setScheduleInterval((short) 0);
        request.setRunTime(null);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> NextScheduledAtCalculationStrategy.estimateNextScheduledAt(request, fixedClock)
        );

        assertEquals("scheduleInterval must be greater than 0", exception.getMessage());
    }

    @Test
    void estimateNextScheduledAt_daily_shouldThrowException_whenRunTimeIsNull() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-06T08:00:00Z"),
                ZoneOffset.UTC
        );
        NextScheduledAtRequest request = new NextScheduledAtRequest();
        request.setScheduleType(ScheduleType.DAILY);
        request.setRunTime(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> NextScheduledAtCalculationStrategy.estimateNextScheduledAt(request, fixedClock));
        assertEquals("Daily schedule requires runTime", exception.getMessage());

    }

    @Test
    void estimateNextScheduledAt_daily_shouldThrowException_whenScheduleIntervalIsNotNull() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-06-06T08:00:00Z"),
                ZoneOffset.UTC
        );
        NextScheduledAtRequest request = new NextScheduledAtRequest();
        request.setScheduleType(ScheduleType.DAILY);
        request.setScheduleInterval((short) 10);
        request.setRunTime(LocalTime.parse("10:00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> NextScheduledAtCalculationStrategy.estimateNextScheduledAt(request, fixedClock));
        assertEquals("INTERVAL schedule must not have scheduleInterval", exception.getMessage());

    }

}