package com.danielvm.receiptprocessor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record Receipt(
    String retailer,
    LocalDate purchaseDate,
    LocalTime purchaseTime,
    List<Item> items,
    BigDecimal total) {

}
