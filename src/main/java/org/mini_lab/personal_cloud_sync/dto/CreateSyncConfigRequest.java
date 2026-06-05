package org.mini_lab.personal_cloud_sync.dto;

import lombok.*;
import org.mini_lab.personal_cloud_sync.enums.ScheduleType;

import java.time.LocalTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CreateSyncConfigRequest {
    private String sourcePath;
    private String targetPath;
    private ScheduleType scheduleType;
    private Short scheduleInterval;
    private LocalTime runTime;
    private Byte maxRetry;
}
