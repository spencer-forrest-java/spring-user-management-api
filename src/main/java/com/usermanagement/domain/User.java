package com.usermanagement.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false, updatable = false)
  private long id;
  private String userId;
  private String firstName;
  private String lastName;
  private String username;
  private String password;
  private String email;
  private String profileImageUrl;
  private Date lastLoginDate;
  private Date lastLoginDisplay;
  private Date joinDate;
  private boolean isActive;
  private boolean isNotLocked;
  @ManyToOne
  private Role role;
}
