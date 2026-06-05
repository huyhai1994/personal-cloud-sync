package org.mini_lab.personal_cloud_sync.services;

import org.mini_lab.personal_cloud_sync.dto.NextScheduledAtRequest;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;

import java.time.*;
import java.util.Optional;

public class NextScheduledAtCalculationStrategy {
    public static Optional<OffsetDateTime> estimateNextScheduledAt(NextScheduledAtRequest nextScheduledAtRequest) {
        return estimateNextScheduledAt(nextScheduledAtRequest, Clock.systemDefaultZone());
    }

    public static Optional<OffsetDateTime> estimateNextScheduledAt(NextScheduledAtRequest nextScheduledAtRequest, Clock clock) {
        ScheduleType scheduleType = nextScheduledAtRequest.getScheduleType() == null ? ScheduleType.MANUAL : nextScheduledAtRequest.getScheduleType();

        Short scheduleInterval = nextScheduledAtRequest.getScheduleInterval();
        LocalTime runTime = nextScheduledAtRequest.getRunTime();

        OffsetDateTime now = OffsetDateTime.now(clock);

        return switch (scheduleType) {
            case MANUAL -> Optional.empty();

            case INTERVAL -> {
                if (runTime != null) {
                    throw new IllegalArgumentException("INTERVAL schedule must not have runTime");
                }

                if (scheduleInterval == null) {
                    throw new IllegalArgumentException("INTERVAL schedule requires scheduleInterval");
                }

                if (scheduleInterval <= 0) {
                    throw new IllegalArgumentException("scheduleInterval must be greater than 0");
                }

                yield Optional.of(now.plusHours(scheduleInterval));
            }

            case DAILY -> {
                if (runTime == null) {
                    throw new IllegalArgumentException("Daily schedule requires runTime");
                }

                if (scheduleInterval != null) {
                    throw new IllegalArgumentException("INTERVAL schedule must not have scheduleInterval");
                }

                OffsetDateTime candidate = OffsetDateTime.of(now.toLocalDate(), runTime, now.getOffset());

                if (!candidate.isAfter(now)) {
                    candidate = candidate.plusDays(1);
                }

                yield Optional.of(candidate);
            }
        };
    }
}
