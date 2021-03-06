package com.usermanagement.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Authority {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false, updatable = false)
  private long id;
  private String name;
}
