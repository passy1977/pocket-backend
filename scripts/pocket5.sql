
CREATE SCHEMA IF NOT EXISTS pocket5;
USE pocket5;

SET foreign_key_checks = 0;

-- pocket5.devices definition

CREATE TABLE `devices` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(256) NOT NULL,
  `version` varchar(255) DEFAULT NULL,
  `status` tinyint(4) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `note` tinytext DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `public_key` longtext NOT NULL,
  `private_key` longtext NOT NULL,
  `timestamp_last_login` bigint(20) NOT NULL DEFAULT 0,
  `timestamp_last_update` bigint(20) NOT NULL DEFAULT 0,
  `timestamp_creation` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `devices_uuid_unique` (`uuid`),
  KEY `idx_devices_device_serial` (`uuid`),
  KEY `FKrfbri1ymrwywdydc4dgywe1bt` (`user_id`),
  CONSTRAINT `FKrfbri1ymrwywdydc4dgywe1bt` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB;

-- pocket5.fields definition

CREATE TABLE `fields` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `timestamp_last_update` bigint(20) DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `group_field_id` bigint(20) NOT NULL,
  `is_hidden` bit(1) NOT NULL,
  `title` varchar(256) NOT NULL,
  `value` varchar(256) NOT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `timestamp_creation` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp0x6ck9vn979mwcmcrwg9p8vl` (`group_id`),
  KEY `FKevkcgfm2ljrikj9ffqgd29d6j` (`user_id`),
  CONSTRAINT `FKevkcgfm2ljrikj9ffqgd29d6j` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKp0x6ck9vn979mwcmcrwg9p8vl` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`)
) ENGINE=InnoDB;

-- pocket5.group_fields definition

CREATE TABLE `group_fields` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `is_hidden` bit(1) NOT NULL,
  `title` varchar(256) NOT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `timestamp_last_update` bigint(20) DEFAULT NULL,
  `timestamp_creation` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5cx8julu52s6rr8rgj58cfnwl` (`group_id`),
  KEY `FKn0m9p9hbw2jgsh7agcxxc2cl6` (`user_id`),
  CONSTRAINT `FK5cx8julu52s6rr8rgj58cfnwl` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`),
  CONSTRAINT `FKn0m9p9hbw2jgsh7agcxxc2cl6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB;

-- pocket5.groups definition

CREATE TABLE `groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `icon` varchar(256) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `title` varchar(256) NOT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `timestamp_last_update` bigint(20) DEFAULT NULL,
  `timestamp_creation` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeck94qxgkh97vnh6qcow1tvy` (`group_id`),
  KEY `FK4cygfv5el2o2v3hbkdkscfw5q` (`user_id`),
  CONSTRAINT `FK4cygfv5el2o2v3hbkdkscfw5q` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKeck94qxgkh97vnh6qcow1tvy` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`)
) ENGINE=InnoDB;

-- pocket5.properties definition

CREATE TABLE `properties` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `_key` varchar(128) NOT NULL,
  `type` tinyint(4) NOT NULL,
  `value` varchar(256) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjqrdpu6sewqivtihk05mcve52` (`user_id`),
  CONSTRAINT `FKjqrdpu6sewqivtihk05mcve52` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB;

-- pocket5.users definition

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(256) NOT NULL,
  `name` varchar(256) NOT NULL,
  `passwd` varchar(256) NOT NULL,
  `status` tinyint(4) NOT NULL,
  `timestamp_creation` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB;

SET foreign_key_checks = 1;