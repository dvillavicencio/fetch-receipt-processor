package com.danielvm.receiptprocessor.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("receipt")
public record ReceiptEntity(@Id Long id, Integer points) {

}
