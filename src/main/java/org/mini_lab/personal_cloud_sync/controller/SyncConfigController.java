package org.mini_lab.personal_cloud_sync.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.services.SyncConfigService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sync-config")
public class SyncConfigController {

    private final SyncConfigService syncConfigService;

    @PostMapping
    ResponseEntity<Short> createSyncConfig(@RequestBody @NonNull CreateSyncConfigRequest request) {
        Short syncConfigId = syncConfigService.createSyncConfig(request);
        return ResponseEntity
                .status(HttpStatusCode.valueOf(201))
                .body(syncConfigId);
    }
}
