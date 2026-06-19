package org.mini_lab.personal_cloud_sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mini_lab.personal_cloud_sync.entities.SyncAttempt;
import org.mini_lab.personal_cloud_sync.repositories.SyncAttemptRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.mini_lab.personal_cloud_sync.repositories.SyncJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonalCloudSyncApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SyncConfigRepository syncConfigRepository;

    @Autowired
    private SyncJobRepository syncJobRepository;

    @TempDir
    Path sourcePath;

    @TempDir
    Path targetPath;

    @BeforeEach
    void setUp() {
        syncAttemptRepository.deleteAllInBatch();
        syncJobRepository.deleteAllInBatch();
        syncConfigRepository.deleteAllInBatch();
    }

    private String validRequestBody() {
        return """
                {
                  "sourcePath": "%s",
                  "targetPath": "%s"
                }
                """.formatted(sourcePath.toString(), targetPath.toString());
    }

    private String requestBodyWithSchedule(String scheduleFields) {
        return """
                {
                  "sourcePath": "%s",
                  "targetPath": "%s",
                  %s
                }
                """.formatted(sourcePath.toString(), targetPath.toString(), scheduleFields);
    }

    @Test
    void create_sync_config_when_max_retry_exceeded_should_return_400() throws Exception {
        String requestBody = """
                {
                  "sourcePath": "%s",
                  "targetPath": "%s",
                  "maxRetry": 10
                }
                """.formatted(sourcePath.toString(), targetPath.toString());

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Maximum Retry Count Exceed should less or equal 5"));
    }

    @Test
    void createManualSyncJob_whenSyncConfigNotExists_shouldReturn404() throws Exception {
        short notExistsSyncConfigId = 999;

        mockMvc.perform(post("/sync-config/{id}/sync-jobs/manual", notExistsSyncConfigId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Sync Config not found!"));
    }

    @Test
    void create_sync_config_when_request_null_should_return_400() throws Exception {
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_sync_config_when_sourcePath_null_should_return_400() throws Exception {
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetPath": "/target/test"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Source/Target Path not valid"));
    }

    @Test
    void create_sync_config_when_targetPath_null_should_return_400() throws Exception {
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourcePath": "/source/test"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Source/Target Path not valid"));
    }

    @Test
    void create_sync_config_when_sourcePath_blank_should_return_400() throws Exception {
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourcePath": "",
                                  "targetPath": "/target/test"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Source/Target Path not valid"));
    }

    @Test
    void create_sync_config_when_targetPath_blank_should_return_400() throws Exception {
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourcePath": "/source/test",
                                  "targetPath": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Source/Target Path not valid"));
    }

    @Test
    void create_sync_config_when_source_path_not_exist_should_return_400() throws Exception {
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourcePath": "/source/test",
                                  "targetPath": "/target/test"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Source/Target Path not valid"));
    }

    @Test
    void create_sync_config_should_return_201() throws Exception {
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void create_sync_config_schedule_type_interval_runtime_not_null_should_return_400() throws Exception {
        String requestBody = requestBodyWithSchedule("""
                "scheduleType":"INTERVAL",
                "runTime":"10:00"
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("INTERVAL schedule must not have runTime"));
    }

    @Test
    void create_sync_config_schedule_type_scheduleInterval_null_should_return_400() throws Exception {
        String requestBody = requestBodyWithSchedule("""
                "scheduleType":"INTERVAL"
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("INTERVAL schedule requires scheduleInterval"));
    }

    @Test
    void create_sync_config_schedule_type_scheduleInterval_lessThan0_shouldReturn400() throws Exception {
        String negativeIntervalRequest = requestBodyWithSchedule("""
                "scheduleType":"INTERVAL",
                "scheduleInterval":-1
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(negativeIntervalRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("scheduleInterval must be greater than 0"));

        String zeroIntervalRequest = requestBodyWithSchedule("""
                "scheduleType":"INTERVAL",
                "scheduleInterval":0
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zeroIntervalRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("scheduleInterval must be greater than 0"));
    }

    @Test
    void create_sync_config_schedule_type_scheduleInterval_shouldReturn201() throws Exception {
        String requestBody = requestBodyWithSchedule("""
                "scheduleType":"INTERVAL",
                "scheduleInterval":30
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void createSyncConfig_scheduleType_daily_shouldReturn400_whenRuntimeNull() throws Exception {
        String requestBody = requestBodyWithSchedule("""
                "scheduleType":"DAILY"
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Daily schedule requires runTime"));
    }

    @Test
    void createSyncConfig_scheduleType_daily_shouldReturn400_whenScheduleIntervalNotNull() throws Exception {
        String requestBody = requestBodyWithSchedule("""
                "scheduleType":"DAILY",
                "runTime":"10:00",
                "scheduleInterval":30
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Daily schedule must not have scheduleInterval"));
    }

    @Test
    void createSyncConfig_scheduleType_daily_shouldReturn201() throws Exception {
        String requestBody = requestBodyWithSchedule("""
                "scheduleType":"DAILY",
                "runTime":"10:00"
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void createSyncConfig_whenDuplicateSyncConfig_ShouldReturn400() throws Exception {
        String requestBody = requestBodyWithSchedule("""
                "scheduleType":"DAILY",
                "runTime":"10:00"
                """);
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                .value("Sync config already exists"));
    }

    @Test
    void createSyncConfig_sameSourceTargetAndSameScheduleType_shouldReturn400() throws Exception {

        String requestBody = requestBodyWithSchedule("""
                "scheduleType":"DAILY",
                "runTime":"10:00"
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Sync config already exists"));
    }

    @Test
    void createSyncConfig_sameSourceTargetButDifferentScheduleType_shouldReturn201() throws Exception {

        String dailyRequest = requestBodyWithSchedule("""
                "scheduleType":"DAILY",
                "runTime":"10:00"
                """);

        String intervalRequest = requestBodyWithSchedule("""
                "scheduleType":"INTERVAL",
                "scheduleInterval":20
                """);

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dailyRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(intervalRequest))
                .andExpect(status().isCreated());
    }
}