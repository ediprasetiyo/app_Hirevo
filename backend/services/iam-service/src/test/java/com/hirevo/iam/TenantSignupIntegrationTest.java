package com.hirevo.iam;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class TenantSignupIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  static GenericContainer<?> redis =
      new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  @DynamicPropertySource
  static void redisProps(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092"); // not started
    registry.add("spring.kafka.producer.bootstrap-servers", () -> "localhost:9092");
    registry.add("spring.autoconfigure.exclude",
        () -> "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
  }

  @Autowired MockMvc mvc;

  @Test
  void signupCreatesTenantAndAdminUser() throws Exception {
    String body = """
        {
          "companyName": "Acme Corp",
          "subdomain": "acme-test",
          "adminEmail": "edi@acme.test",
          "adminPassword": "SecurePass123!",
          "adminFullName": "Edi P"
        }
        """;
    mvc.perform(post("/v1/tenants/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.tenantId", notNullValue()))
        .andExpect(jsonPath("$.subdomain").value("acme-test"))
        .andExpect(jsonPath("$.adminUserId", notNullValue()));
  }

  @Test
  void signupRejectsDuplicateSubdomain() throws Exception {
    String body = """
        {
          "companyName": "First",
          "subdomain": "unique-slug",
          "adminEmail": "a@x.test",
          "adminPassword": "SecurePass123!",
          "adminFullName": "A"
        }
        """;
    mvc.perform(post("/v1/tenants/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated());
    mvc.perform(post("/v1/tenants/signup").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict());
  }
}
