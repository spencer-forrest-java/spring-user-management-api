package com.usermanagement.exception.domain;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.usermanagement.domain.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.Objects;

@RestControllerAdvice
@RestController
public class ExceptionHandling implements ErrorController {

  private static final String ACCOUNT_DISABLED =
      "Your account has been disabled. If this is an error, please contact the administrator";
  private static final String ACCOUNT_LOCKED = "Your account has been locked. Please contact administration.";
  private static final String ERROR_PATH = "/error";
  private static final String ERROR_PROCESSING_FILE = "Error occurred while processing file";
  private static final String INCORRECT_CREDENTIALS = "Username / password incorrect. Please try again.";
  private static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";
  private static final String METHOD_IS_NOT_ALLOWED =
      "This method request is not allowed on this endpoint. Please send a '%s' request";
  private static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission to perform this action";
  private static final String PICTURE_BIGGER_THAN_1_MB = "Image's size is more than 1 MB.";

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<HttpResponse> accountDisableException() {
    return createHttpResponse(HttpStatus.BAD_REQUEST, ACCOUNT_DISABLED);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<HttpResponse> badCredentialsException() {
    return createHttpResponse(HttpStatus.BAD_REQUEST, INCORRECT_CREDENTIALS);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<HttpResponse> accessDeniedException() {
    return createHttpResponse(HttpStatus.FORBIDDEN, NOT_ENOUGH_PERMISSION);
  }

  @ExceptionHandler(NotImageException.class)
  public ResponseEntity<HttpResponse> notImageException(NotImageException exception) {
    return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<HttpResponse> maxUploadSizeExceededException() {
    return createHttpResponse(HttpStatus.BAD_REQUEST, PICTURE_BIGGER_THAN_1_MB);
  }

  @ExceptionHandler(RoleUpdateException.class)
  public ResponseEntity<HttpResponse> roleUpdateException(RoleUpdateException exception) {
    return createHttpResponse(HttpStatus.FORBIDDEN, exception.getMessage());
  }

  @ExceptionHandler(SpecialAdminUpdateException.class)
  public ResponseEntity<HttpResponse> specialAdminUpdateException(SpecialAdminUpdateException exception) {
    return createHttpResponse(HttpStatus.FORBIDDEN, exception.getMessage());
  }

  @ExceptionHandler(SpecialAdminDeleteException.class)
  public ResponseEntity<HttpResponse> specialAdminDeleteException(SpecialAdminDeleteException exception) {
    return createHttpResponse(HttpStatus.FORBIDDEN, exception.getMessage());
  }

  @ExceptionHandler(SpecialAdminResetPasswordException.class)
  public ResponseEntity<HttpResponse> specialAdminResetPasswordException(SpecialAdminResetPasswordException exception) {
    return createHttpResponse(HttpStatus.FORBIDDEN, exception.getMessage());
  }

  @ExceptionHandler(LockedException.class)
  public ResponseEntity<HttpResponse> lockedException() {
    return createHttpResponse(HttpStatus.UNAUTHORIZED, ACCOUNT_LOCKED);
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception) {
    return createHttpResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  @ExceptionHandler(EmailExistException.class)
  public ResponseEntity<HttpResponse> emailExistException(EmailExistException exception) {
    return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(UsernameExistException.class)
  public ResponseEntity<HttpResponse> usernameExistException(UsernameExistException exception) {
    return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(EmailNotFoundException.class)
  public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception) {
    return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception) {
    return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
    HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
    return createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
  }

  @ExceptionHandler(NoResultException.class)
  public ResponseEntity<HttpResponse> notFoundExceptionException(Exception exception) {
    logger.error(exception.getMessage());
    return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<HttpResponse> iOException(Exception exception) {
    logger.error(exception.getMessage());
    return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception) {
    logger.error(exception.getMessage());
    return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
  }

  @RequestMapping(ERROR_PATH)
  public ResponseEntity<HttpResponse> notFoundError() {
    return createHttpResponse(HttpStatus.NOT_FOUND, "Invalid path.");
  }

  /**
   * Returns the path of the error page.
   *
   * @return the error path
   */
  @Override
  public String getErrorPath() {
    return ERROR_PATH;
  }

  private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus status, String message) {
    String upperCaseReason = status.getReasonPhrase().toUpperCase();
    return new ResponseEntity<>(new HttpResponse(status.value(), status, upperCaseReason, message), status);
  }
}
