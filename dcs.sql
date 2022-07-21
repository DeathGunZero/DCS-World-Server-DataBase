/*
 Navicat Premium Data Transfer

 Source Server         : Microsoft Server
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : 192.168.5.100:3306
 Source Schema         : dcs

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 21/07/2022 14:01:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for player
-- ----------------------------
DROP TABLE IF EXISTS `player`;
CREATE TABLE `player`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `ucid` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `pts` int NOT NULL DEFAULT 0,
  `splashAA` int NOT NULL DEFAULT 0,
  `splashAG` int NOT NULL DEFAULT 0,
  `splashSEAD` int NOT NULL DEFAULT 0,
  `dead` int NOT NULL DEFAULT 0,
  `landing` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ucid_index`(`ucid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for player_seq
-- ----------------------------
DROP TABLE IF EXISTS `player_seq`;
CREATE TABLE `player_seq`  (
  `next_val` bigint NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
