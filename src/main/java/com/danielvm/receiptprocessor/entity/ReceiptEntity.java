package com.danielvm.receiptprocessor.entity;

import org.springframework.data.annotation.Id;

public record ReceiptEntity(@Id Long id, Integer points) {

}
