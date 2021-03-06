-- ---------------------------
-- Table structure for `heroes`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `heroes` (
  `charId` decimal(11,0) NOT NULL DEFAULT 0,
  `char_name` varchar(45) NOT NULL DEFAULT '',
  `class_id` decimal(3,0) NOT NULL DEFAULT 0,
  `count` decimal(3,0) NOT NULL DEFAULT 0,
  `played` decimal(1,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
) DEFAULT CHARSET=utf8;