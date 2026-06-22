package org.mini_lab.personal_cloud_sync.component;

import org.mini_lab.personal_cloud_sync.dto.NextScheduledAtRequest;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;

import java.time.*;
import java.util.Optional;

public class NextScheduledAtCalculationStrategy {
    private NextScheduledAtCalculationStrategy() {
    }

    public static Optional<OffsetDateTime> estimateNextScheduledAt(NextScheduledAtRequest nextScheduledAtRequest, Clock clock) {
        ScheduleType scheduleType = nextScheduledAtRequest.getScheduleType();

        Short scheduleInterval = nextScheduledAtRequest.getScheduleInterval();
        LocalTime runTime = nextScheduledAtRequest.getRunTime();
        OffsetDateTime now = OffsetDateTime.now(clock);

        return switch (scheduleType) {
            case MANUAL -> Optional.empty();

            case INTERVAL -> Optional.of(now.plusMinutes(scheduleInterval));

            case DAILY -> {
                OffsetDateTime candidate = OffsetDateTime.of(now.toLocalDate(), runTime, now.getOffset());

                if (!candidate.isAfter(now)) {
                    candidate = candidate.plusDays(1);
                }

                yield Optional.of(candidate);
            }
        };
    }
}
