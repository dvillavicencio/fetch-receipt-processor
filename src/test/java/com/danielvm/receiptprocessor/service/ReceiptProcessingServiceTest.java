package com.danielvm.receiptprocessor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.danielvm.receiptprocessor.dto.Item;
import com.danielvm.receiptprocessor.dto.Receipt;
import com.danielvm.receiptprocessor.entity.ReceiptEntity;
import com.danielvm.receiptprocessor.repository.ReceiptRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ReceiptProcessingServiceTest {

  @Mock
  ReceiptRepository repository;

  @InjectMocks
  ReceiptProcessingService sut;

  /**
   * 3 items -> 1 pairs of items = 5 points
   * </br>
   * Coke Zero has length 9. 0.2 * 2.99 = 0.598 -> 1 point
   * </br>
   * 'Target' has 6 alphanumeric chars = 6 points
   * </br>
   * Total = 1 + 5 + 6 = 12 points
   */
  private static @NotNull Receipt getSampleReceipt() {
    List<Item> items = List.of( // 3 items ->  5 points
        new Item("Coke Zero", new BigDecimal("2.99")), // strlength = 9, 2.9 + 0.2 == 1 point
        new Item("Kit Kat (BIG)", new BigDecimal("8.00")), // No points
        new Item(" Napolitan Ice Cream  ", new BigDecimal("12.83")) // No points
    );
    return new Receipt(
        "Target", // 6 points
        LocalDate.of(2024, 7, 30), // no points
        LocalTime.of(13, 1), // no points
        items,
        new BigDecimal("23.82"));
  }

  /**
   * 5.99 * 0.2 = 1.198 -> 1 point
   * </br>
   * 573.12 * 0.2 = 114.624 -> 115 points
   * </br>
   * There's two pairs of items = 10 points
   * </br>
   * Total = 1 + 115 + 10 = 126 points
   */
  private static @NotNull Receipt getItemsReceipt() {
    List<Item> items = List.of(
        new Item("  Organic Eggs  ", new BigDecimal("5.99")), // length 12
        new Item("This String is supposed to be a big digit of three hopefully  ",
            new BigDecimal("573.12")), // length 60
        new Item("Skibidi", new BigDecimal("142.01")), // not divisible by three
        new Item(" Napolitan Ice Cream  ", new BigDecimal("12.83")) // not divisible by three
    );
    return new Receipt("", LocalDate.of(2024, 11, 30), LocalTime.of(9, 0), items,
        new BigDecimal("83.11"));
  }

  /**
   * 'Walmart' has 7 alphanumeric characters = 7 points
   * </br>
   * Total of 81.00 is divisible by 0.25 = 25 points
   * </br>
   * Total = 7 + 25 = 32 points
   */
  private static @NotNull Receipt getDivisibleBy25Receipt() {
    return new Receipt("Walmart", // 7 points
        LocalDate.of(2024, 7, 30), LocalTime.of(9, 0),
        Collections.emptyList(),
        new BigDecimal("81.25") // 50 points + 25 points
    );
  }

  /**
   * '--=-=/``2' has only 1 alphanumeric char = 1 point
   * </br>
   * Total = 1 point
   */
  private static @NotNull Receipt getAlphanumericReceipt() {
    return new Receipt("--=-=/``2", // 1 point
        LocalDate.of(2024, 7, 30), LocalTime.of(9, 0),
        Collections.emptyList(),
        new BigDecimal("81.12")
    );
  }

  /**
   * Total of the receipt has no cents in the decimal place = 50 points
   * </br>
   * Total of 92.00 is also divisible by 0.25 = 25 points
   * </br>
   * Total = 50 + 25 = 50 points
   */
  private static @NotNull Receipt getNoCentsReceipt() {
    return new Receipt("", LocalDate.of(2024, 7, 30), LocalTime.of(9, 0), Collections.emptyList(),
        new BigDecimal("92.00"));
  }

  @Test
  void process_receipt_should_successfully_validate_receipt() {
    // given: a sample receipt
    ArgumentCaptor<ReceiptEntity> argumentCaptor = ArgumentCaptor.forClass(ReceiptEntity.class);
    Receipt receipt = getSampleReceipt();

    when(repository.save(any())).thenReturn(new ReceiptEntity(1L, 12));

    // when: process receipt is called
    sut.processReceipt(receipt);

    // then: the database entity should have 12 points in total
    verify(repository, times(1)).save(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().points()).isEqualTo(12);
  }

  @Test
  void should_validate_divisible_by_25_rule() {
    // given: a receipt with a total that's only divisible by 0.25
    ArgumentCaptor<ReceiptEntity> argumentCaptor = ArgumentCaptor.forClass(ReceiptEntity.class);
    Receipt receipt = getDivisibleBy25Receipt();
    when(repository.save(any())).thenReturn(new ReceiptEntity(1L, 57));

    // when: process receipt is called
    sut.processReceipt(receipt);

    // then: the database entity should have 32 points
    verify(repository, times(1)).save(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().points()).isEqualTo(32);
  }

  @Test
  void should_validate_alphanumeric_rule() {
    // given: a receipt with a retailer with only one alphanumeric character
    ArgumentCaptor<ReceiptEntity> argumentCaptor = ArgumentCaptor.forClass(ReceiptEntity.class);
    Receipt receipt = getAlphanumericReceipt();

    when(repository.save(any())).thenReturn(new ReceiptEntity(1L, 1));

    // when: process receipt is called
    sut.processReceipt(receipt);

    // then: the database entity should have 1 point
    verify(repository, times(1)).save(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().points()).isEqualTo(1);
  }

  @Test
  void should_validate_item_descriptions() {
    // given: a receipt with items that are both divisible and non-divisible by 3 and untrimmed
    ArgumentCaptor<ReceiptEntity> argumentCaptor = ArgumentCaptor.forClass(ReceiptEntity.class);
    Receipt receipt = getItemsReceipt();

    when(repository.save(any())).thenReturn(new ReceiptEntity(1L, 126));

    // when: process receipt is called
    sut.processReceipt(receipt);

    // then: the database entity should have 126 points
    verify(repository, times(1)).save(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().points()).isEqualTo(126);
  }

  @Test
  void should_validate_no_cents_rule() {
    // given: a receipt with items that only has total with no cents
    ArgumentCaptor<ReceiptEntity> argumentCaptor = ArgumentCaptor.forClass(ReceiptEntity.class);
    Receipt receipt = getNoCentsReceipt();

    when(repository.save(any())).thenReturn(new ReceiptEntity(1L, 50));

    // when: process receipt is called
    sut.processReceipt(receipt);

    // then: the database entity should have 75 points
    verify(repository, times(1)).save(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().points()).isEqualTo(75);
  }
}
