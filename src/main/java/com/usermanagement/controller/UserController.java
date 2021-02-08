package com.usermanagement.controller;

import com.usermanagement.domain.HttpResponse;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserPrincipal;
import com.usermanagement.dto.UserDto;
import com.usermanagement.exception.domain.*;
import com.usermanagement.service.UserService;
import com.usermanagement.utility.FileConstant;
import com.usermanagement.utility.JWTUtility;
import com.usermanagement.utility.SecurityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200", exposedHeaders = {"Jwt-Token"})
public class UserController {

  private static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final JWTUtility jwtUtility;

  @Autowired
  public UserController(UserService userService,
                        AuthenticationManager authenticationManager,
                        JWTUtility jwtUtility) {

    this.userService = userService;
    this.authenticationManager = authenticationManager;
    this.jwtUtility = jwtUtility;
  }

  @PostMapping("/register")
  public ResponseEntity<UserDto> register(@RequestBody UserDto user)
      throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {

    User newUser = this.userService.register(user.getFirstName(),
                                             user.getLastName(),
                                             user.getUsername(),
                                             user.getEmail());

    return new ResponseEntity<>(new UserDto(newUser), HttpStatus.OK);
  }

  @PostMapping("/login")
  public ResponseEntity<UserDto> login(@RequestBody UserDto user) {

    System.out.println("user: " + user);

    this.authenticate(user.getUsername(), user.getPassword());

    User loginUser = userService.findByUserName(user.getUsername());
    UserPrincipal userPrincipal = new UserPrincipal(loginUser);
    HttpHeaders jwtHeader = this.getJwtHeader(userPrincipal);

    System.out.println("jwt header: " + jwtHeader);

    return new ResponseEntity<>(new UserDto(loginUser), jwtHeader, HttpStatus.OK);
  }

  @PreAuthorize("hasAuthority('user:create')")
  @PostMapping("/add")
  public ResponseEntity<UserDto> addUser(@RequestParam("newUsername") String newUsername,
                                         @RequestParam("newFirstName") String newFirstName,
                                         @RequestParam("newLastName") String newLastName,
                                         @RequestParam("newEmail") String newEmail,
                                         @RequestParam("newRole") String newRole,
                                         @RequestParam("active") String isActive,
                                         @RequestParam("notLocked") String isNotLocked,
                                         @RequestParam(value = "profileImage",
                                                       required = false) MultipartFile profileImage)
      throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotImageException {

    User newUser = userService.addNewUser(newFirstName,
                                          newLastName,
                                          newUsername,
                                          newEmail,
                                          newRole,
                                          Boolean.parseBoolean(isNotLocked),
                                          Boolean.parseBoolean(isActive),
                                          profileImage);
    return new ResponseEntity<>(new UserDto(newUser), HttpStatus.OK);
  }

  @PutMapping("/update")
  public ResponseEntity<UserDto> updateUser(@RequestParam("currentUsername") String currentUsername,
                                            @RequestParam("newUsername") String newUsername,
                                            @RequestParam("newFirstName") String newFirstName,
                                            @RequestParam("newLastName") String newLastName,
                                            @RequestParam("newEmail") String newEmail,
                                            @RequestParam("newRole") String newRole,
                                            @RequestParam("active") String isActive,
                                            @RequestParam("notLocked") String isNotLocked,
                                            @RequestParam(value = "profileImage",
                                                          required = false) MultipartFile profileImage,
                                            Authentication authentication)
      throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotImageException,
             SuperAdminUpdateException, RoleUpdateException {

    boolean isAuthorizeToUpdate = this.authorizeToUpdate(currentUsername, authentication);

    User updatedUser = userService.updateUser(currentUsername,
                                              newUsername,
                                              newFirstName,
                                              newLastName,
                                              newEmail,
                                              newRole,
                                              Boolean.parseBoolean(isActive),
                                              Boolean.parseBoolean(isNotLocked),
                                              profileImage,
                                              isAuthorizeToUpdate);

    return new ResponseEntity<>(new UserDto(updatedUser), HttpStatus.OK);
  }

  @PutMapping("/update/profile-image")
  public ResponseEntity<UserDto> updateProfileImage(@RequestParam("username") String username,
                                                    @RequestParam(value = "profileImage") MultipartFile profileImage,
                                                    Authentication authentication)
      throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotImageException {

    this.authorizeToUpdate(username, authentication);

    User updatedUser = userService.updateProfileImage(username, profileImage);
    return new ResponseEntity<>(new UserDto(updatedUser), HttpStatus.OK);
  }

  @PreAuthorize("hasAuthority('user:read')")
  @GetMapping("/list")
  public ResponseEntity<List<UserDto>> getAllUser() {
    List<UserDto> users = this.userService.getUsers().stream().map(UserDto::new).collect(Collectors.toList());
    return new ResponseEntity<>(users, HttpStatus.OK);
  }

  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  @GetMapping("/reset-password/{email}")
  public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email,
                                                    Authentication authentication)
      throws EmailNotFoundException, MessagingException {
    this.userService.resetPassword(email, authentication);
    return createResponseEntity("Reset password sent to: " + email);
  }

  @GetMapping(path = "/image/{userId}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
  public byte[] getProfileImage(@PathVariable("userId") long userId, @PathVariable("fileName") String fileName)
      throws IOException {
    Path path =
        Paths.get(FileConstant.DESKTOP_FOLDER + userId + FileConstant.FORWARD_SLASH + fileName);
    return Files.readAllBytes(path);
  }

  @GetMapping(path = "/image/profile/{username}", produces = MediaType.IMAGE_JPEG_VALUE)
  public byte[] getTemporaryProfileImage(@PathVariable("username") String username)
      throws IOException {
    URL url = new URL(FileConstant.TEMP_PROFILE_BASED_URL + username);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    try (InputStream inputStream = url.openConnection().getInputStream()) {
      int bytesRead;
      byte[] chunk = new byte[1024];
      while ((bytesRead = inputStream.read(chunk)) > 0) {
        byteArrayOutputStream.write(chunk, 0, bytesRead);
      }
    }

    return byteArrayOutputStream.toByteArray();
  }

  @PreAuthorize("hasAuthority('user:delete')")
  @DeleteMapping("/delete/{username}")
  public ResponseEntity<HttpResponse> deleteUser(@PathVariable("username") String username)
      throws IOException, SuperUserDeleteException {
    this.userService.deleteUser(username);
    return this.createResponseEntity(USER_DELETED_SUCCESSFULLY);
  }

  /**
   * Create an {@link UsernamePasswordAuthenticationToken} object; then attempts to authenticate it, returning a
   * fully populated <code>Authentication</code> object (including granted authorities)
   * if successful.
   */
  private void authenticate(String username, String password) {
    this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
  }

  private boolean authorizeToUpdate(String currentUsername, Authentication authentication) {

    boolean hasUpdateAuthority = authentication.getAuthorities().stream()
                                               .anyMatch(authority -> authority.getAuthority().equals("user:update"));

    if (!authentication.getPrincipal().equals(currentUsername) && !hasUpdateAuthority) {
      throw new AccessDeniedException(null);
    }

    return hasUpdateAuthority;
  }

  private ResponseEntity<HttpResponse> createResponseEntity(String message) {
    return new ResponseEntity<>(new HttpResponse(HttpStatus.OK.value(),
                                                 HttpStatus.OK,
                                                 HttpStatus.OK.getReasonPhrase(),
                                                 message), HttpStatus.OK);
  }

  private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(SecurityConstant.JWT_TOKEN_HEADER, jwtUtility.generateJwtToken(userPrincipal));
    return httpHeaders;
  }
}
