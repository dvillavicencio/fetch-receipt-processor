package com.danielvm.receiptprocessor.controller;

import com.danielvm.receiptprocessor.dto.PointsProcessResponse;
import com.danielvm.receiptprocessor.dto.PointsResponse;
import com.danielvm.receiptprocessor.dto.Receipt;
import com.danielvm.receiptprocessor.service.ReceiptProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReceiptController {

  private static final Logger log = LoggerFactory.getLogger(ReceiptController.class);

  private final ReceiptProcessingService receiptProcessingService;

  public ReceiptController(ReceiptProcessingService receiptProcessingService) {
    this.receiptProcessingService = receiptProcessingService;
  }

  /**
   * Process a receipt
   *
   * @param receipt the receipt object
   * @return the ID of the saved receipt entity
   */
  @PostMapping("/receipts/process")
  public ResponseEntity<PointsProcessResponse> processReceipt(
      @RequestBody Receipt receipt) {
    log.info("Processing receipt...");
    var response = receiptProcessingService.processReceipt(receipt);
    log.info("Finished processing receipt!");
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieve points for a receipt
   *
   * @param id receipt ID
   * @return {@link PointsResponse}
   */
  @GetMapping("/receipts/{id}/points")
  public ResponseEntity<PointsResponse> getPoints(@PathVariable Long id) {
    log.info("Retrieving points for receipt with id: [{}]", id);
    var response = receiptProcessingService.getPoints(id);
    log.info("Retrieving points for receipt with id: [{}]", id);
    return ResponseEntity.ok(response);
  }
}
