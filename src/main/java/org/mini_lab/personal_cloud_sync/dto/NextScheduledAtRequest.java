package org.mini_lab.personal_cloud_sync.dto;

import lombok.*;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NextScheduledAtRequest {
    private ScheduleType scheduleType;
    private LocalTime runTime;
    private Short scheduleInterval;
}
