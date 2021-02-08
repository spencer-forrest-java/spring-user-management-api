package com.usermanagement.utility;

public class SecurityConstant {

  public static final String ACCESS_DENIED_MESSAGE = "You do not have the permission to access this page";
  public static final String AUTHORITIES = "authorities";
  public static final long EXPIRATION_TIME = 86_400_000; // 1 day in ms
  public static final String FORBIDDEN_MESSAGE = "You need to login to access this page";
  public static final String JWT_TOKEN_HEADER = "Jwt-Token";
  public static final String MY_COMPANY = "My company";
  public static final String MY_COMPANY_ADMINISTRATION = "User Management";
  public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
  public static final String[] PUBLIC_URLS = {
      "/user/login", "/user/register", "/user/image/**"
  };
  public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
  public static final String TOKEN_PREFIX = "Bearer ";
}
