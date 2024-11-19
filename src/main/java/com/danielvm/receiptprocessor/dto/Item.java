package com.danielvm.receiptprocessor.dto;

import java.math.BigDecimal;

public record Item(
    String shortDescription,
    BigDecimal price) {

}
