package org.mini_lab.personal_cloud_sync.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RCloneResult {
    private int exitCode;
    private String errorMessage;

    public boolean isSuccess() {
        return exitCode == 0;
    }
}
