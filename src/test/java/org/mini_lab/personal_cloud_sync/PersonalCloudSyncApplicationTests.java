package org.mini_lab.personal_cloud_sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mini_lab.personal_cloud_sync.repositories.SyncConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

    @BeforeEach
    void setUp() {
        syncConfigRepository.deleteAll();
    }

    @Test
    void create_sync_config_when_max_retry_exceeded_should_return_400() throws Exception {
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourcePath": "/source/test",
                                  "targetPath": "/target/test",
                                  "maxRetry": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Maximum Retry Count Exceed should less or equal 5"));
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
                        .content("""
                                {
                                  "sourcePath": "/home/huyhai1994/workspace/backend-notes",
                                  "targetPath": "/workspace/backend-notes"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void create_sync_config_schedule_type_interval_runtime_not_null_should_return_400() throws Exception {

        String requestBody =
                """
                        {
                          "sourcePath": "/home/huyhai1994/workspace/backend-notes",
                          "targetPath": "/workspace/backend-notes",
                          "scheduleType":"INTERVAL",
                          "runTime":"10:00"
                        }
                        """;
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("INTERVAL schedule must not have runTime"));
    }

    @Test
    void create_sync_config_schedule_type_scheduleInterval__null_should_return_400() throws Exception {

        String requestBody =
                """
                        {
                          "sourcePath": "/home/huyhai1994/workspace/backend-notes",
                          "targetPath": "/workspace/backend-notes",
                          "scheduleType":"INTERVAL"
                        }
                        """;
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("INTERVAL schedule requires scheduleInterval"));
    }

    @Test
    void create_sync_config_schedule_type_scheduleInterval__lessThan0_shouldReturn400() throws Exception {

        String requestBody =
                """
                        {
                          "sourcePath": "/home/huyhai1994/workspace/backend-notes",
                          "targetPath": "/workspace/backend-notes",
                          "scheduleType":"INTERVAL",
                          "scheduleInterval":-1
                        }
                        """;
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("scheduleInterval must be greater than 0"));

        requestBody =
                """
                        {
                          "sourcePath": "/home/huyhai1994/workspace/backend-notes",
                          "targetPath": "/workspace/backend-notes",
                          "scheduleType":"INTERVAL",
                          "scheduleInterval":0
                        }
                        """;
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("scheduleInterval must be greater than 0"));
    }

    @Test
    void create_sync_config_schedule_type_scheduleInterval_shouldReturn201() throws Exception {

        String requestBody =
                """
                        {
                          "sourcePath": "/home/huyhai1994/workspace/backend-notes",
                          "targetPath": "/workspace/backend-notes",
                          "scheduleType":"INTERVAL",
                          "scheduleInterval":30
                        }
                        """;
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void createSyncConfig_scheduleType_daily_shouldReturn400_whenRuntimeNull() throws Exception {

        String requestBody =
                """
                        {
                          "sourcePath": "/home/huyhai1994/workspace/backend-notes",
                          "targetPath": "/workspace/backend-notes",
                          "scheduleType":"DAILY"
                        }
                        """;
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

        String requestBody =
                """
                        {
                          "sourcePath": "/home/huyhai1994/workspace/backend-notes",
                          "targetPath": "/workspace/backend-notes",
                          "scheduleType":"DAILY",
                          "runTime":"10:00",
                          "scheduleInterval":30
                        }
                        """;
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

        String requestBody =
                """
                        {
                          "sourcePath": "/home/huyhai1994/workspace/backend-notes",
                          "targetPath": "/workspace/backend-notes",
                          "scheduleType":"DAILY",
                          "runTime":"10:00"
                        }
                        """;
        mockMvc.perform(post("/sync-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated());
    }
}
