package com.usermanagement.service;

import com.usermanagement.domain.Role;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserPrincipal;
import com.usermanagement.exception.domain.*;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.usermanagement.utility.FileConstant.*;

@Service
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

  private static final String EMAIL_ALREADY_TAKEN_BY_ANOTHER_USER = "Email already taken by another user";
  private static final String NO_USER_FOUND_BY_EMAIL = "No user found for email: ";
  private static final String SUPER_USER_USERNAME = "admin";
  private static final String USERNAME_ALREADY_TAKEN_BY_ANOTHER_USER = "Username already taken by another user";
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final LoginAttemptService loginAttemptService;
  private final EmailService emailService;
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private String superAdminUsername;
  private Role userRole;

  @Autowired
  public UserServiceImpl(UserRepository userRepository,
                         RoleRepository roleRepository,
                         LoginAttemptService loginAttemptService,
                         EmailService emailService,
                         BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.loginAttemptService = loginAttemptService;
    this.emailService = emailService;
    this.roleRepository = roleRepository;
  }

  @PostConstruct
  public void postConstructor() {
    User user = this.userRepository.findUserByUsername(SUPER_USER_USERNAME);
    this.superAdminUsername = user == null ? "" : user.getUsername();
    this.userRole = this.roleRepository.findByName("ROLE_USER");
  }

  /**
   * Locates the user based on the username. In the actual implementation, the search
   * may possibly be case sensitive, or case insensitive depending on how the
   * implementation instance is configured. In this case, the <code>UserDetails</code>
   * object that comes back may have a username that is of a different case than what
   * was actually requested..
   *
   * @param username the username identifying the user whose data is required.
   * @return a fully populated user record (never <code>null</code>)
   * @throws UsernameNotFoundException if the user could not be found or the user has no
   *                                   GrantedAuthority
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = this.userRepository.findUserByUsername(username);
    if (user == null) {
      logger.error("User not found by username: " + username);
      throw new UsernameNotFoundException("User not found by username: " + username);
    } else {
      this.validateLoginAttempt(user);
      user.setLastLoginDisplay(user.getLastLoginDate());
      user.setLastLoginDate(new Date());
      this.userRepository.save(user);
      return new UserPrincipal(user);
    }
  }

  @Override
  public User register(String firstName, String lastName, String username, String email)
      throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {

    this.validateNewUsernameAndEmail(null, username, email);

    String password = this.generatePassword();

    User user = createUser(firstName, lastName, username, email, password);
    user.setRole(this.userRole);
    this.userRepository.save(user);
    this.emailService.sendEmail(firstName, password, email);

    logger.info("user password: " + password);
    return user;
  }

  @Override
  public User addNewUser(String firstName,
                         String lastName,
                         String username,
                         String email,
                         String role,
                         boolean isNotLocked,
                         boolean isActive,
                         MultipartFile profileImage)
      throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotImageException {

    String password = this.generatePassword();
    this.validateNewUsernameAndEmail(null, username, email);
    User user = createUser(firstName, lastName, username, email, password);
    user.setNotLocked(isNotLocked);
    user.setActive(isActive);
    user.setRole(this.getRole(role));
    this.userRepository.save(user);
    this.saveProfileImage(user, profileImage);

    logger.info("user password: " + password);
    return user;
  }

  @Override
  public User updateUser(String currentUsername,
                         String newUsername,
                         String newFirstName,
                         String newLastName,
                         String newEmail,
                         String newRole,
                         boolean isActive,
                         boolean isNotLocked,
                         MultipartFile profileImage,
                         boolean hasUpdateAuthority)
      throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotImageException,
             SuperAdminUpdateException, RoleUpdateException {

    // forbid super user to be updated
    if (this.superAdminUsername.equals(currentUsername)) {
      throw new SuperAdminUpdateException("It is forbidden to update this user");
    }

    User currentUser = Objects.requireNonNull(this.validateNewUsernameAndEmail(currentUsername, newUsername, newEmail));

    // forbid user with no update authority to update role
    if (!hasUpdateAuthority && !currentUser.getRole().getName().equalsIgnoreCase(newRole)) {
      throw new RoleUpdateException("You are not authorized to update your role");
    }

    currentUser.setFirstName(newFirstName);
    currentUser.setLastName(newLastName);
    currentUser.setUsername(newUsername);
    currentUser.setEmail(newEmail);
    currentUser.setActive(isActive);
    currentUser.setNotLocked(isNotLocked);
    if (hasUpdateAuthority) {
      currentUser.setRole(this.getRole(newRole));
    }
    this.userRepository.save(currentUser);
    this.saveProfileImage(currentUser, profileImage);

    return currentUser;
  }

  @Override
  public User updateProfileImage(String username, MultipartFile profileImage)
      throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotImageException {
    User user = this.validateNewUsernameAndEmail(username, null, null);
    this.saveProfileImage(user, profileImage);
    return user;
  }

  @Override
  public User findByUserName(String username) {
    return this.userRepository.findUserByUsername(username);
  }

  @Override
  public User findByEmail(String email) {
    return this.userRepository.findUserByEmail(email);
  }

  @Override
  public List<User> getUsers() {
    return this.userRepository.getUsers();
  }

  @Override
  public void deleteUser(String username) throws IOException, SuperUserDeleteException {
    if (this.superAdminUsername.equals(username)) {
      throw new SuperUserDeleteException("It is forbidden to delete this user");
    }
    this.deleteProfileImage(username);
    this.userRepository.deleteUserByUsername(username);
  }

  @Override
  public void resetPassword(String email, Authentication authentication)
      throws EmailNotFoundException, MessagingException {

    User user = this.findByEmail(email);
    if (user == null) {
      throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
    }
    String password = this.generatePassword();
    user.setPassword(this.passwordEncoder.encode(password));
    this.userRepository.save(user);
    this.emailService.sendEmail(user.getFirstName(), password, email);
    logger.info("user password: " + password);
  }

  private User createUser(String firstName, String lastName, String username, String email, String password) {

    String encodedPassword = this.passwordEncoder.encode(password);

    User user = new User();
    user.setUserId(this.generateId());
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(encodedPassword);
    user.setProfileImageUrl(this.getTemporaryImageURL(username));
    user.setActive(true);
    user.setNotLocked(true);
    user.setJoinDate(new Date());

    return user;
  }

  private void deleteProfileImage(String username) throws IOException {
    User user = userRepository.findUserByUsername(username);
    if (user != null) {
      Path profileImageDirectory = Paths.get(DESKTOP_FOLDER + user.getUserId()).toAbsolutePath().normalize();
      FileUtils.deleteDirectory(new File(profileImageDirectory.toString()));
      logger.info("Directory deleted: " + profileImageDirectory.toAbsolutePath().toString());
    }
  }

  private String generateId() {
    return RandomStringUtils.randomNumeric(10);
  }

  private String generatePassword() {
    return RandomStringUtils.randomAlphanumeric(10);
  }

  private String getProfileUrl(String userId) {
    return ServletUriComponentsBuilder.fromCurrentContextPath()
                                      .path(USER_IMAGE_PATH + userId + FORWARD_SLASH + userId + DOT + JPG_EXTENSION)
                                      .toUriString();
  }

  private Role getRole(String role) {
    return this.roleRepository.findByName(role);
  }

  private String getTemporaryImageURL(String username) {
    return ServletUriComponentsBuilder.fromCurrentContextPath()
                                      .path(USER_IMAGE_PROFILE_PATH + username)
                                      .toUriString();
  }

  private void saveProfileImage(User user, MultipartFile profileImage) throws IOException, NotImageException {
    if (profileImage != null) {
      if (!Arrays.asList(MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE)
                 .contains(profileImage.getContentType())) {
        throw new NotImageException(profileImage.getOriginalFilename() + " is not an image. Please upload an image");
      }

      Path userFolder = Paths.get(DESKTOP_FOLDER + user.getUserId()).toAbsolutePath().normalize();

      if (!Files.exists(userFolder)) {
        Files.createDirectories(userFolder);
        logger.info(DIRECTORY_CREATED + userFolder);
      }

      Path imagePath = Paths.get(userFolder + FORWARD_SLASH + user.getUserId() + DOT + JPG_EXTENSION);
      Files.deleteIfExists(imagePath);
      Files.copy(profileImage.getInputStream(), imagePath);

      user.setProfileImageUrl(this.getProfileUrl(user.getUserId()));
      userRepository.save(user);
      logger.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
    }
  }

  private void validateLoginAttempt(User user) {
    if (user.isNotLocked()) {
      if (loginAttemptService.isMaxCountReached(user.getUsername())) {
        user.setNotLocked(false);
      }
    } else {
      loginAttemptService.removeUserFromCache(user.getUsername());
    }
  }

  private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail)
      throws UsernameExistException, EmailExistException, UserNotFoundException {

    User userByUsername = findByUserName(newUsername);
    User userByEmail = findByEmail(newEmail);

    // check 'email' and 'new username' for user with 'current username'
    if (StringUtils.isNotBlank(currentUsername)) {
      User currentUser = findByUserName(currentUsername);
      // check if current user not found
      if (currentUser == null) {
        throw new UserNotFoundException("No user found with username: " + currentUsername);
      }
      // check if new user exists
      if (userByUsername != null && userByUsername.getId() != currentUser.getId()) {
        throw new UsernameExistException(USERNAME_ALREADY_TAKEN_BY_ANOTHER_USER);
      }
      // check if email exists
      if (userByEmail != null && userByEmail.getId() != currentUser.getId()) {
        throw new EmailExistException(EMAIL_ALREADY_TAKEN_BY_ANOTHER_USER);
      }
      return currentUser;
    } else { // else statement check the same parameters but for new user.
      // check if new user exists
      if (userByUsername != null) {
        throw new UsernameExistException(USERNAME_ALREADY_TAKEN_BY_ANOTHER_USER);
      }
      // check if email exists
      if (userByEmail != null) {
        throw new EmailExistException(EMAIL_ALREADY_TAKEN_BY_ANOTHER_USER);
      }
      return null;
    }
  }
}
