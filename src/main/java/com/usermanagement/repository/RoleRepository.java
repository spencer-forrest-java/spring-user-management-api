package com.usermanagement.repository;

import com.usermanagement.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Role findByName(String name);
}
