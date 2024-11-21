package com.danielvm.receiptprocessor.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.danielvm.receiptprocessor.entity.ReceiptEntity;
import com.danielvm.receiptprocessor.repository.ReceiptRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ReceiptControllerTest {

  // Test containers setup for PostgresQL
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
      DockerImageName.parse("postgres:latest"))
      .withDatabaseName("receipt")
      .withUsername("username")
      .withPassword("password");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ReceiptRepository receiptRepository;

  // Postgresql will run in a random port, therefore we cannot specify in the
  // application.yml which port it will run so we do it specify it here dynamically
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url",
        () -> "jdbc:postgresql://localhost:" + postgreSQLContainer.getFirstMappedPort() + "/"
            + postgreSQLContainer.getDatabaseName());
    registry.add("spring.datasource.username", () -> postgreSQLContainer.getUsername());
    registry.add("spring.datasource.password", () -> postgreSQLContainer.getPassword());
  }

  @AfterAll
  static void stopDatabase() {
    postgreSQLContainer.stop();
  }

  @Test
  @Transactional
  void should_process_receipt() throws Exception {
    // given: a valid receipt
    Resource resource = new ClassPathResource("__files/receipt.json");

    // when: the request is sent
    RequestBuilder request = MockMvcRequestBuilders.post("/receipts/process")
        .contentType(MediaType.APPLICATION_JSON)
        .content(resource.getContentAsByteArray());
    var response = mockMvc.perform(request);

    // then: the points result is saved to the DB with the correct amount of points
    Iterator<ReceiptEntity> receipts = receiptRepository.findAll().iterator();
    List<ReceiptEntity> list = new ArrayList<>();
    while (receipts.hasNext()) {
      list.add(receipts.next());
    }
    assertThat(list).hasSize(1);
    assertThat(list.getFirst().points()).isEqualTo(28);

    // and: the response is 200 OK with the ID of the points entity
    response.andDo(MockMvcResultHandlers.print());
    response.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    response.andExpect(jsonPath("$.id", is(list.getFirst().id().intValue())));

  }

  @Test
  @Transactional
  void should_return_receipt_points() throws Exception {
    // given: a valid ID of a receipt already present in the DB
    receiptRepository.save(new ReceiptEntity(null, 36));
    receiptRepository.save(new ReceiptEntity(null, 17));
    receiptRepository.save(new ReceiptEntity(null, 12));

    Long receiptID = 1L;

    // when: the request is sent
    RequestBuilder request = MockMvcRequestBuilders.get("/receipts/{receiptID}/points", receiptID)
        .contentType(MediaType.APPLICATION_JSON);
    var response = mockMvc.perform(request);

    // then: the points result is returned from the DB for the given receiptID
    var entity = receiptRepository.findById(receiptID);

    response.andDo(MockMvcResultHandlers.print());
    response.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    response.andExpect(jsonPath("$.points", is(entity.get().points())));
  }

  @Test
  @Transactional
  void should_return_error_on_receipt_not_found() throws Exception {
    // given: an invalid ID of a receipt not present in the DB
    receiptRepository.save(new ReceiptEntity(null, 36));
    receiptRepository.save(new ReceiptEntity(null, 17));
    receiptRepository.save(new ReceiptEntity(null, 12));

    Long receiptID = 4L;

    // when: the request is sent
    RequestBuilder request = MockMvcRequestBuilders.get("/receipts/{receiptID}/points", receiptID)
        .contentType(MediaType.APPLICATION_JSON);
    var response = mockMvc.perform(request);

    // then: the corresponding error message is returned with the correct error message
    response.andDo(MockMvcResultHandlers.print());
    response.andExpect(MockMvcResultMatchers.status().is4xxClientError());
    response.andExpect(
        jsonPath("$.message", is("Receipt with ID [%s] could not be found".formatted(receiptID))));
    response.andExpect(jsonPath("$.route", is("/receipts/%s/points".formatted(receiptID))));
    response.andExpect(jsonPath("$.httpStatus", is("NOT_FOUND")));
  }

}
