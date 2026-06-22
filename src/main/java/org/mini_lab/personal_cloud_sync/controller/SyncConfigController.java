package org.mini_lab.personal_cloud_sync.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.dto.SyncJobResponse;
import org.mini_lab.personal_cloud_sync.services.SyncJobService;
import org.mini_lab.personal_cloud_sync.services.SyncConfigService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/sync-config")
public class SyncConfigController {

    private final SyncConfigService syncConfigService;
    private final SyncJobService manualSyncJobService;

    @PostMapping
    ResponseEntity<Short> createSyncConfig(@RequestBody @NonNull CreateSyncConfigRequest request) {
        Short syncConfigId = syncConfigService.createSyncConfig(request);
        return ResponseEntity
                .status(HttpStatusCode.valueOf(201))
                .body(syncConfigId);
    }

    @PostMapping("/{id}/sync-jobs/manual")
    ResponseEntity<SyncJobResponse> createManualSyncJob(@PathVariable @NonNull Short id) {
        log.info("REQUEST_RECEIVED syncConfigId={}", id);
        SyncJobResponse syncJobResponse = manualSyncJobService.createAndDispatch(id);
        return ResponseEntity
                .status(HttpStatusCode.valueOf(201))
                .body(syncJobResponse);
    }
}
