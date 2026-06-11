package org.mini_lab.personal_cloud_sync.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mini_lab.personal_cloud_sync.component.ISyncConfigValidator;
import org.mini_lab.personal_cloud_sync.component.SyncConfigMapper;
import org.mini_lab.personal_cloud_sync.dto.CreateSyncConfigRequest;
import org.mini_lab.personal_cloud_sync.entities.SyncConfig;
import org.mini_lab.personal_cloud_sync.exception.DuplicateSyncConfigException;
import org.mini_lab.personal_cloud_sync.exception.InternalServerException;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncConfigService {

    private final SyncConfigRepository syncConfigRepository;

    private final ISyncConfigValidator syncConfigValidator;

    private final SyncConfigMapper syncConfigMapper;

    @Transactional
    public Short createSyncConfig(CreateSyncConfigRequest request) {
        syncConfigValidator.validateCreateSyncConfigRequest(request);
        SyncConfig syncConfig = syncConfigMapper.mapCreateSyncConfigRequestToSyncConfig(request);
        try {
            SyncConfig persistedSyncConfig = syncConfigRepository.save(syncConfig);
            return persistedSyncConfig.getId();

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSyncConfigException();

        } catch (TransactionTimedOutException | DataAccessException e) {
            throw new InternalServerException();
        }
    }
}
