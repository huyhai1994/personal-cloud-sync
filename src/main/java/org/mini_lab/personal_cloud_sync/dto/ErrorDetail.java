package org.mini_lab.personal_cloud_sync.dto;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {
    private String message;
}
