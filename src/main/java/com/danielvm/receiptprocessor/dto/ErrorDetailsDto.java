package com.danielvm.receiptprocessor.dto;

import org.springframework.http.HttpStatus;

public record ErrorDetailsDto(String route, String message, HttpStatus httpStatus) {

}
