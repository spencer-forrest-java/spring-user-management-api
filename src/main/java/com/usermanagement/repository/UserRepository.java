package com.usermanagement.repository;

import com.usermanagement.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {
	@Query("select u from User u where u.username<>'admin'")
	List<User> getUsers();
	User findUserByUsername(String username);
	User findUserByEmail(String email);
	void deleteUserByUsername(String username);
}
