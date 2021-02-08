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

  // needed for jackson
//	public String getTimeStamp() {
//		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z");
//		ZoneId zoneId = ZoneId.of("Europe/Paris");
//		ZonedDateTime zonedDateTime = this.timeStamp.atZone(zoneId);
//		return dateTimeFormatter.format(zonedDateTime);
//	}
}
