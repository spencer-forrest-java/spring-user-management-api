package com.usermanagement.listener;

import com.usermanagement.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener {

  public static final String SPECIAL_ADMIN_USERNAME = "admin";
  private final LoginAttemptService loginAttemptService;

  @Autowired
  public AuthenticationFailureListener(LoginAttemptService loginAttemptService) {
    this.loginAttemptService = loginAttemptService;
  }

  @EventListener
  public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
    Object principal = event.getAuthentication().getPrincipal();
    if (principal instanceof String) {
      String username = (String) principal;
      // Does not apply to special admin
      if (!username.equalsIgnoreCase(SPECIAL_ADMIN_USERNAME)) {
        loginAttemptService.addUserToCache(username);
      }
    }
  }
}
