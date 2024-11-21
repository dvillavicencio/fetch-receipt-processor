package com.danielvm.receiptprocessor.exception;

import lombok.Data;

@Data
public class ReceiptNotFoundException extends RuntimeException {

  public ReceiptNotFoundException(String message) {
    super(message);
  }
}
