package com.danielvm.receiptprocessor.controller;

import com.danielvm.receiptprocessor.dto.ErrorDetailsDto;
import com.danielvm.receiptprocessor.exception.ReceiptNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles errors of type {@link ReceiptNotFoundException}
   *
   * @param e       the exception
   * @param request the original request
   * @return HTTP 404 response
   */
  @ExceptionHandler(ReceiptNotFoundException.class)
  public ResponseEntity<ErrorDetailsDto> handleException(HttpServletRequest request,
      ReceiptNotFoundException e) {
    ErrorDetailsDto errorDetailsDto = new ErrorDetailsDto(request.getRequestURI(), e.getMessage(),
        HttpStatus.NOT_FOUND);
    return new ResponseEntity<>(errorDetailsDto, HttpStatus.NOT_FOUND);
  }
}
