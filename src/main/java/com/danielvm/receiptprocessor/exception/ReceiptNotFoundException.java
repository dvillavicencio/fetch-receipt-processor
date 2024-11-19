package com.danielvm.receiptprocessor.exception;

public class ReceiptNotFoundException extends RuntimeException {

  private String message;

  public ReceiptNotFoundException(String message) {
    super(message);
  }
}
