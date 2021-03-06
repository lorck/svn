-- ---------------------------
-- Table structure for `character_skills`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `character_skills` (
  `charId` INT UNSIGNED NOT NULL DEFAULT 0,
  `skill_id` INT NOT NULL DEFAULT 0,
  `skill_level` INT(3) NOT NULL default 1,
  `skill_name` varchar(40),
  `class_index` INT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`skill_id`,`class_index`)
) DEFAULT CHARSET=utf8;