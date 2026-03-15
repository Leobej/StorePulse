package org.projects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projects.domain.ImportStatus;
import org.projects.domain.ImportType;
import org.projects.persistence.entity.ImportBatch;
import org.projects.persistence.entity.Store;
import org.projects.repository.AlertRecordRepository;
import org.projects.repository.AppUserRepository;
import org.projects.repository.ImportBatchRepository;
import org.projects.repository.ImportRowErrorRepository;
import org.projects.repository.SalesDailyAggregateRepository;
import org.projects.repository.SalesRecordRepository;
import org.projects.repository.StoreRepository;
import org.projects.repository.UserStoreAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImportControllerIntegrationTest {
    private static final Path STORAGE_DIR = createTempDirectory();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImportBatchRepository batchRepository;

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    @Autowired
    private ImportRowErrorRepository importRowErrorRepository;

    @Autowired
    private SalesDailyAggregateRepository salesDailyAggregateRepository;

    @Autowired
    private AlertRecordRepository alertRecordRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserStoreAccessRepository userStoreAccessRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:storepulse;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("storepulse.import.storage-dir", () -> STORAGE_DIR.toString());
        registry.add("storepulse.auth.bootstrap-username", () -> "admin");
        registry.add("storepulse.auth.bootstrap-password", () -> "storepulse-admin");
        registry.add("storepulse.auth.jwt-secret", () -> "U3RvcmVQdWxzZVN0b3JlUHVsc2VTdG9yZVB1bHNlU2VjcmV0MTIzNDU2");
        registry.add("storepulse.auth.jwt-expiration", () -> "PT12H");
    }

    @BeforeEach
    void cleanDatabase() {
        salesRecordRepository.deleteAll();
        importRowErrorRepository.deleteAll();
        alertRecordRepository.deleteAll();
        salesDailyAggregateRepository.deleteAll();
        batchRepository.deleteAll();
    }

    @Test
    void uploadsProcessesPersistsAndSummarizesSalesImport() throws Exception {
        String token = login();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sales.csv",
            MediaType.TEXT_PLAIN_VALUE,
            """
                receipt_id,sold_at,product_sku,product_name,quantity,unit_price,total_amount
                R-1,2026-03-15T10:15:30+02:00,SKU-1,Apple,2,3.50,7.00
                R-1,2026-03-15T10:17:00+02:00,SKU-2,Banana,1,1.20,1.20
                R-2,invalid-date,SKU-3,Orange,1,2.00,2.00
                """.getBytes()
        );

        String responseBody = mockMvc.perform(multipart("/imports/sales").file(file).header("Authorization", bearer(token)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode response = objectMapper.readTree(responseBody);
        UUID batchId = UUID.fromString(response.get("id").asText());

        ImportBatch batch = waitForCompletion(batchId);
        assertEquals(ImportStatus.COMPLETED, batch.getImportStatus());
        assertEquals(3, batch.getProcessedRows());
        assertEquals(2, batch.getSuccessfulRows());
        assertEquals(1, batch.getFailedRows());
        assertNotNull(batch.getUpdatedAt());

        mockMvc.perform(get("/imports/{batchId}", batchId).header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.processedRows").value(3))
            .andExpect(jsonPath("$.successfulRows").value(2))
            .andExpect(jsonPath("$.failedRows").value(1));

        mockMvc.perform(get("/imports/{batchId}/sales-records", batchId).header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].receiptId").value("R-1"))
            .andExpect(jsonPath("$[1].productName").value("Banana"));

        mockMvc.perform(get("/imports/{batchId}/row-errors", batchId).header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].lineNumber").value(4));

        mockMvc.perform(get("/analytics/sales-summary").param("batchId", batchId.toString()).header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.revenue").value(8.20))
            .andExpect(jsonPath("$.unitsSold").value(3))
            .andExpect(jsonPath("$.receipts").value(1))
            .andExpect(jsonPath("$.averageBasket").value(8.20))
            .andExpect(jsonPath("$.topProducts[0].productName").value("Apple"))
            .andExpect(jsonPath("$.topProducts[0].unitsSold").value(2))
            .andExpect(jsonPath("$.hourlyDistribution[10].revenue").value(8.20));

        waitForAnalyticsMaterialization();

        mockMvc.perform(get("/dashboard/overview").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dailyAggregates.length()").value(1))
            .andExpect(jsonPath("$.dailyAggregates[0].revenue").value(8.20))
            .andExpect(jsonPath("$.openAlerts.length()").value(2));

        mockMvc.perform(get("/imports").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(batchId.toString()));

        String alertsBody = mockMvc.perform(get("/alerts").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID alertId = UUID.fromString(objectMapper.readTree(alertsBody).get(0).get("id").asText());
        mockMvc.perform(post("/alerts/{alertId}/acknowledge", alertId).header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));
    }

    @Test
    void rejectsDuplicateImportByChecksum() throws Exception {
        String token = login();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sales.csv",
            MediaType.TEXT_PLAIN_VALUE,
            """
                receipt_id,sold_at,product_sku,product_name,quantity,unit_price,total_amount
                R-1,2026-03-15T10:15:30+02:00,SKU-1,Apple,2,3.50,7.00
                """.getBytes()
        );

        mockMvc.perform(multipart("/imports/sales").file(file).header("Authorization", bearer(token)))
            .andExpect(status().isCreated());

        ImportBatch createdBatch = waitForSingleBatch();
        waitForCompletion(createdBatch.getId());

        mockMvc.perform(multipart("/imports/sales").file(file).header("Authorization", bearer(token)))
            .andExpect(status().isConflict());
    }

    @Test
    void rejectsProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/dashboard/overview"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsBearerToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"storepulse-admin"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.currentStore.code").value("default-store"))
            .andExpect(jsonPath("$.allowedStores.length()").value(1));
    }

    @Test
    void bootstrapsPersistedAdminUser() {
        org.junit.jupiter.api.Assertions.assertTrue(appUserRepository.findByUsername("admin").isPresent());
    }

    @Test
    void hidesImportsFromOtherStores() throws Exception {
        String token = login();
        Store otherStore = storeRepository.save(new Store(
            UUID.randomUUID(),
            "other-store",
            "Other Store",
            true,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        ));

        ImportBatch foreignBatch = batchRepository.save(new ImportBatch(
            UUID.randomUUID(),
            otherStore,
            ImportType.SALES,
            ImportStatus.COMPLETED,
            "C:\\temp\\other.csv",
            "other.csv",
            "foreign-checksum",
            OffsetDateTime.now()
        ));

        mockMvc.perform(get("/imports/{batchId}", foreignBatch.getId()).header("Authorization", bearer(token)))
            .andExpect(status().isNotFound());
    }

    @Test
    void switchesActiveStoreWhenUserHasAccess() throws Exception {
        String token = login();
        var admin = appUserRepository.findByUsername("admin").orElseThrow();
        Store secondStore = storeRepository.save(new Store(
            UUID.randomUUID(),
            "second-store",
            "Second Store",
            true,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        ));
        userStoreAccessRepository.save(new org.projects.persistence.entity.UserStoreAccess(
            UUID.randomUUID(),
            admin,
            secondStore,
            OffsetDateTime.now()
        ));

        ImportBatch foreignBatch = batchRepository.save(new ImportBatch(
            UUID.randomUUID(),
            secondStore,
            ImportType.SALES,
            ImportStatus.COMPLETED,
            "C:\\temp\\second.csv",
            "second.csv",
            "second-checksum",
            OffsetDateTime.now()
        ));

        mockMvc.perform(post("/auth/switch-store")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"storeId":"%s"}
                    """.formatted(secondStore.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentStore.code").value("second-store"))
            .andExpect(jsonPath("$.allowedStores.length()").value(2));

        mockMvc.perform(get("/imports").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(foreignBatch.getId().toString()))
            .andExpect(jsonPath("$[0].storeId").value(secondStore.getId().toString()));
    }

    @Test
    void returnsCurrentSessionForAuthenticatedUser() throws Exception {
        String token = login();

        mockMvc.perform(get("/auth/me").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.currentStore.code").value("default-store"))
            .andExpect(jsonPath("$.allowedStores[0].code").value("default-store"));
    }

    private ImportBatch waitForCompletion(UUID batchId) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            ImportBatch batch = batchRepository.findById(batchId).orElseThrow();
            if (batch.getImportStatus() == ImportStatus.COMPLETED || batch.getImportStatus() == ImportStatus.FAILED) {
                return batch;
            }
            Thread.sleep(25L);
        }

        throw new AssertionError("Import batch did not complete in time");
    }

    private static Path createTempDirectory() {
        try {
            return Files.createTempDirectory("storepulse-import-test");
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create temp directory", ex);
        }
    }

    private ImportBatch waitForSingleBatch() throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            if (batchRepository.count() == 1) {
                return batchRepository.findAll().getFirst();
            }
            Thread.sleep(20L);
        }

        throw new AssertionError("Batch was not persisted in time");
    }

    private void waitForAnalyticsMaterialization() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            if (salesDailyAggregateRepository.count() == 1 && alertRecordRepository.count() == 2) {
                return;
            }
            Thread.sleep(25L);
        }

        throw new AssertionError("Analytics materialization did not complete in time");
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"storepulse-admin"}
                    """))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
