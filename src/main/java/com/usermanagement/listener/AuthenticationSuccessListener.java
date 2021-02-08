package com.usermanagement.listener;

import com.usermanagement.domain.UserPrincipal;
import com.usermanagement.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener {

  private final LoginAttemptService loginAttemptService;

  @Autowired
  public AuthenticationSuccessListener(LoginAttemptService loginAttemptService) {
    this.loginAttemptService = loginAttemptService;
  }

  @EventListener
  public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    Object principal = event.getAuthentication().getPrincipal();
    if (principal instanceof UserPrincipal) {
      UserPrincipal user = (UserPrincipal) event.getAuthentication().getPrincipal();
      loginAttemptService.removeUserFromCache(user.getUsername());
    }
  }
}
