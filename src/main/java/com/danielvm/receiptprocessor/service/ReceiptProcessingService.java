package com.danielvm.receiptprocessor.service;

import com.danielvm.receiptprocessor.dto.PointsProcessResponse;
import com.danielvm.receiptprocessor.dto.PointsResponse;
import com.danielvm.receiptprocessor.dto.Receipt;
import com.danielvm.receiptprocessor.entity.ReceiptEntity;
import com.danielvm.receiptprocessor.exception.ReceiptNotFoundException;
import com.danielvm.receiptprocessor.repository.ReceiptRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReceiptProcessingService {

  private static final Logger log = LoggerFactory.getLogger(ReceiptProcessingService.class);

  final ReceiptRule itemDescriptionRule = r -> {
    if (!(r.items() == null || r.items().isEmpty())) {
      return r.items().stream()
          .filter(item -> {
            if (item.shortDescription().trim().length() % 3 == 0) {
              log.debug("Item [{}] description's length is divisible by 3",
                  item.shortDescription());
              return true;
            } else {
              log.debug("Item [{}] description is not divisible by 3", item.shortDescription());
              return false;
            }
          })
          .mapToInt(item -> {
            var mult = item.price().multiply(new BigDecimal("0.2")).doubleValue();
            var result = Math.toIntExact(Math.round(mult));
            log.info(
                "Item [{}] price multiplied by 0.2 is [{}]. Rounded up to [{}]. +{} points",
                item.shortDescription(), mult, result, result);
            return result;
          })
          .sum();
    } else {
      log.info("No items found in the receipt. +0 points");
      return 0;
    }
  };

  final ReceiptRule purchaseDateRule = r -> {
    if (r.purchaseDate().getDayOfMonth() % 2 != 0) {
      log.info("Purchase date's day is an odd number. +6 points");
      return 6;
    } else {
      log.info("Purchase date's day is NOT an odd number. +0 points");
      return 0;
    }
  };

  final ReceiptRule purchaseTimeRule = r -> {
    if (r.purchaseTime().isAfter(LocalTime.of(14, 0)) && r.purchaseTime()
        .isBefore(LocalTime.of(16, 0))) {
      log.info("Receipt purchase time is between 2:00pm and 4:00pm. +10 points");
      return 10;
    } else {
      log.info("Receipt purchase time is NOT between 2:00pm and 4:00pm. +0 points");
      return 0;
    }
  };

  final ReceiptRule alphanumericRule = r -> {
    if (Objects.isNull(r.retailer()) || r.retailer().isEmpty()) {
      log.warn("Retailer is either null or empty. +0 points");
      return 0;
    } else {
      int result = r.retailer().chars()
          .map(c -> {
            if (Character.isLetterOrDigit(c)) {
              log.debug("Character [{}] for retailer [{}] is alphanumeric. +1 point", c,
                  r.retailer());
              return 1;
            } else {
              log.debug("Character [{}] for retailer [{}] is NOT alphanumeric. +0 points", c,
                  r.retailer());
              return 0;
            }
          }).sum();
      log.info("Retailer [{}] has a total of {} alphanumeric characters. +{} points", r.retailer(),
          result, result);
      return result;
    }
  };

  final ReceiptRule noCentsRule = r -> {
    if (r.total().unscaledValue().longValue() > 0
        && getDecimalPart(r.total()) == 0) {
      log.info("Receipt with retailer [{}] has zero cents in total value. +50 points",
          r.retailer());
      return 50;
    } else {
      log.info("Receipt with retailer [{}] has non-zero cents in total value. +0 points",
          r.retailer());
      return 0;
    }
  };

  final ReceiptRule twentyFiveCentsRule = r -> {
    if (r.total().doubleValue() % 0.25 == 0) {
      log.info("Total value [{}] is divisible by 0.25. +25 points", r.total());
      return 25;
    } else {
      log.info("Total value [{}] is NOT divisible by 0.25. +0 points", r.total());
      return 0;
    }
  };

  final ReceiptRule twoItemsRule = r -> {
    if (r.items() == null || r.items().isEmpty()) {
      return 0;
    } else {
      int numberOfPairs = r.items().size() / 2;
      int result = numberOfPairs * 5;
      log.info("There are {} pairs of two. +{} points", numberOfPairs, result);
      return result;
    }
  };

  private final List<ReceiptRule> rulesList = List.of(
      alphanumericRule, noCentsRule, twentyFiveCentsRule, twoItemsRule,
      itemDescriptionRule, purchaseDateRule, purchaseTimeRule
  );

  private final ReceiptRepository receiptRepository;

  public ReceiptProcessingService(ReceiptRepository receiptRepository) {
    this.receiptRepository = receiptRepository;
  }

  private static int getDecimalPart(BigDecimal bigDecimal) {
    BigDecimal number = bigDecimal.subtract(bigDecimal.setScale(0, RoundingMode.DOWN));
    String numberStringForm = number.toPlainString().replace("0.", "");
    return Integer.parseInt(numberStringForm);
  }

  /**
   * Process a receipt and subject it to all the rules defined above
   *
   * @param receipt the receipt object
   * @return the ID of the database object created
   */
  public PointsProcessResponse processReceipt(Receipt receipt) {
    int points = rulesList.stream()
        .mapToInt(r -> r.apply(receipt)).sum(); // apply all rules to the receipt
    log.info("Total point(s) for receipt for retailer [{}] are: {} points", receipt.retailer(),
        points);
    var createdEntity = receiptRepository.save(new ReceiptEntity(null, points));
    return new PointsProcessResponse(createdEntity.id());
  }

  /**
   * Retrieves the points of a receipt based on a given ID
   *
   * @param id the ID of the receipt to look for
   * @return {@link PointsResponse}
   * @throws ReceiptNotFoundException if no matching receipt is found
   */
  public PointsResponse getPoints(Long id) {
    var receipt = receiptRepository.findById(id);
    if (receipt.isPresent()) {
      return new PointsResponse(receipt.get().points());
    } else {
      throw new ReceiptNotFoundException("Receipt with ID [%s] could not be found".formatted(id));
    }
  }

  /**
   * Interface to better express each rule individually
   */
  @FunctionalInterface
  private interface ReceiptRule {

    int apply(Receipt receipt);
  }
}
