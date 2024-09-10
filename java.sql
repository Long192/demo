-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.39 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for java
CREATE DATABASE IF NOT EXISTS `java` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `java`;

-- Dumping structure for table java.comment
CREATE TABLE IF NOT EXISTS `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `post_id` bigint DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table java.comment: ~0 rows (approximately)
DELETE FROM `comment`;
INSERT INTO `comment` (`id`, `user_id`, `post_id`, `content`, `created_at`, `updated_at`) VALUES
	(1, 1, 3, 'comment for post 3', '2024-09-10 10:15:10', '2024-09-10 10:15:10');

-- Dumping structure for table java.favourite
CREATE TABLE IF NOT EXISTS `favourite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `favourite_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
  CONSTRAINT `favourite_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table java.favourite: ~2 rows (approximately)
DELETE FROM `favourite`;
INSERT INTO `favourite` (`id`, `post_id`, `user_id`, `created_at`, `updated_at`) VALUES
	(1, 3, 1, '2024-09-10 10:14:53', '2024-09-10 10:14:53'),
	(2, 3, 2, '2024-09-10 10:22:33', '2024-09-10 10:22:33');

-- Dumping structure for table java.flyway_schema_history
CREATE TABLE IF NOT EXISTS `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table java.flyway_schema_history: ~2 rows (approximately)
DELETE FROM `flyway_schema_history`;
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES
	(1, '1', 'Initial', 'SQL', 'V1__Initial.sql', -217705558, 'root', '2024-09-10 10:05:11', 146, 1),
	(2, '2', 'add refresh token table', 'SQL', 'V2__add_refresh_token_table.sql', -1190170122, 'root', '2024-09-10 10:05:11', 24, 1);

-- Dumping structure for table java.friend
CREATE TABLE IF NOT EXISTS `friend` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_requester_id` bigint DEFAULT NULL,
  `user_receiver_id` bigint DEFAULT NULL,
  `status` enum('pending','accepted') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_requester_id` (`user_requester_id`),
  KEY `user_receiver_id` (`user_receiver_id`),
  CONSTRAINT `friend_ibfk_1` FOREIGN KEY (`user_requester_id`) REFERENCES `user` (`id`),
  CONSTRAINT `friend_ibfk_2` FOREIGN KEY (`user_receiver_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table java.friend: ~0 rows (approximately)
DELETE FROM `friend`;
INSERT INTO `friend` (`id`, `user_requester_id`, `user_receiver_id`, `status`, `created_at`, `updated_at`) VALUES
	(3, 2, 1, 'accepted', '2024-09-10 10:21:24', '2024-09-10 10:21:33');

-- Dumping structure for table java.image
CREATE TABLE IF NOT EXISTS `image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint DEFAULT NULL,
  `url` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `post_id` (`post_id`),
  CONSTRAINT `image_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table java.image: ~0 rows (approximately)
DELETE FROM `image`;
INSERT INTO `image` (`id`, `post_id`, `url`, `created_at`, `updated_at`) VALUES
	(1, 1, 'https://ucarecdn.com/93833fd6-a444-4186-8342-2964a6d770c5/', '2024-09-10 10:09:42', '2024-09-10 10:09:41'),
	(2, 2, 'https://ucarecdn.com/93833fd6-a444-4186-8342-2964a6d770c5/', '2024-09-10 10:12:13', '2024-09-10 10:12:13');

-- Dumping structure for table java.post
CREATE TABLE IF NOT EXISTS `post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `status` enum('PRIVATE','PUBLIC','FRIEND_ONLY') COLLATE utf8mb4_unicode_ci DEFAULT 'PUBLIC',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `post_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table java.post: ~0 rows (approximately)
DELETE FROM `post`;
INSERT INTO `post` (`id`, `user_id`, `content`, `status`, `created_at`, `updated_at`) VALUES
	(1, 1, 'content', 'PUBLIC', '2024-09-10 10:09:42', '2024-09-10 10:09:42'),
	(2, 1, 'content', 'PUBLIC', '2024-09-10 10:12:13', '2024-09-10 10:12:13'),
	(3, 1, 'content 1', 'FRIEND_ONLY', '2024-09-10 10:14:26', '2024-09-10 10:14:26'),
	(4, 1, 'content 1', 'PRIVATE', '2024-09-10 10:14:35', '2024-09-10 10:14:35');

-- Dumping structure for table java.refresh_token
CREATE TABLE IF NOT EXISTS `refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `token` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `expired_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `refresh_token_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table java.refresh_token: ~2 rows (approximately)
DELETE FROM `refresh_token`;
INSERT INTO `refresh_token` (`id`, `user_id`, `token`, `created_at`, `expired_at`) VALUES
	(1, 1, '3b54ad2d-e26b-4a21-8fff-7d7854d1eedf', '2024-09-10 10:06:49', '2024-10-10 20:36:49'),
	(2, 2, '0be35830-5b6b-423f-8822-cf2abfd1f883', '2024-09-10 10:15:59', '2024-10-10 20:45:59');

-- Dumping structure for table java.user
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fullname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `etc` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `role` enum('admin','user') COLLATE utf8mb4_unicode_ci DEFAULT 'user',
  `status` enum('active','unactive') COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table java.user: ~0 rows (approximately)
DELETE FROM `user`;
INSERT INTO `user` (`id`, `email`, `password`, `fullname`, `address`, `etc`, `avatar`, `dob`, `role`, `status`, `created_at`, `updated_at`) VALUES
	(1, 'email@email.com', '$2a$10$OEbGWjA7cBMbtt/WMkX7m.KPMzME290xrKd/P.sbhVTA6i9lyeUAG', NULL, NULL, NULL, NULL, NULL, 'user', 'active', '2024-09-10 10:06:01', '2024-09-10 10:06:01'),
	(2, 'test@test.com', '$2a$10$pTALX5X0vg/HjE7aRgpY2eTCuqUvcQTIyjyhDd9l2AZxzFagGrO2e', NULL, NULL, NULL, NULL, NULL, 'user', 'active', '2024-09-10 10:15:42', '2024-09-10 10:15:42');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
