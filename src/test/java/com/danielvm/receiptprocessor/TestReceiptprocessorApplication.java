package com.danielvm.receiptprocessor;

import org.springframework.boot.SpringApplication;

public class TestReceiptprocessorApplication {

  public static void main(String[] args) {
    SpringApplication.from(ReceiptprocessorApplication::main)
        .with(TestcontainersConfiguration.class).run(args);
  }

}
