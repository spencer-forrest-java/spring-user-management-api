SET FOREIGN_KEY_CHECKS=0;
drop table if exists authority;

CREATE TABLE `authority` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO authority (name) VALUES ('user:create'), ('user:read'), ('user:update'), ('user:delete');

drop table if exists role;

CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO role (name) VALUES ('ROLE_USER'), ('ROLE_MANAGER'), ('ROLE_ADMIN'), ('ROLE_SUPER_ADMIN');

drop table if exists role_authority;

CREATE TABLE `role_authority` (
  `role_id` bigint(20) NOT NULL,
  `authority_id` bigint(20) NOT NULL,
  KEY `FK_ROLE` (`role_id`),
  KEY `FK_AUTHORITY` (`authority_id`),
  CONSTRAINT `FK_AUTHORITY` FOREIGN KEY (`authority_id`) REFERENCES `authority` (`id`),
  CONSTRAINT `FK_ROLE` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO role_authority (role_id, authority_id) 
VALUES (1,2),(2,2),(2,3),(3,1),(3,2),(3,3),(4,1),(4,2),(4,3),(4,4);

drop table if exists user;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `is_not_locked` bit(1) NOT NULL,
  `join_date` datetime(6) DEFAULT NULL,
  `last_login_date` datetime(6) DEFAULT NULL,
  `last_login_display` datetime(6) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `profile_image_url` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `role_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_USER_ROLE` (`role_id`),
  CONSTRAINT `FK_USER_ROLE` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- password encrypted with BCrypt: 'Rt3wFigGUe'
INSERT INTO user (username, password, email, first_name, last_name, is_active, is_not_locked, user_id, role_id) 
VALUES ('admin','$2y$10$aFAqNeXGjlJwoLnZJeqjqeDjX5Yt7cgECxrbyiiJwBwoPGEsmewly', 'email@example.com', 'admin', 'admin', true, true,'1234567890', 4);

SET FOREIGN_KEY_CHECKS=1;
