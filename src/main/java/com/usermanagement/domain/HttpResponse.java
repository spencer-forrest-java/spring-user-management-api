package com.usermanagement.domain;

import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@ToString
public class HttpResponse {

  private int httpStatusCode;
  private HttpStatus httpStatus;
  private String reason;
  private String message;
  private LocalDateTime timeStamp;

  public HttpResponse(int httpStatusCode, HttpStatus httpStatus, String reason, String message) {
    this.httpStatusCode = httpStatusCode;
    this.httpStatus = httpStatus;
    this.reason = reason;
    this.message = message;
    this.timeStamp = LocalDateTime.now();
  }
}
