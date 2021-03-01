package com.usermanagement.service;

import com.usermanagement.domain.User;
import com.usermanagement.exception.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {

  User register(String firstName, String LastName, String username, String email)
      throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException, IOException,
             NotImageException;

  User addNewUser(String firstName,
                  String lastName,
                  String username,
                  String email,
                  String role,
                  boolean isNotLocked,
                  boolean isActive,
                  MultipartFile profileImage)
      throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotImageException;

  User updateUser(String currentUsername,
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
             SpecialAdminUpdateException, RoleUpdateException;

  User updateProfileImage(String username, MultipartFile profileImage)
      throws UserNotFoundException, UsernameExistException, EmailExistException, IOException, NotImageException;

  User findByUserName(String username);

  User findByEmail(String email);

  List<User> getUsers();

  void deleteUser(String username) throws IOException, SpecialAdminDeleteException;

  void resetPassword(String email, Authentication authentication)
      throws EmailNotFoundException, MessagingException, SpecialAdminResetPasswordException;
}
