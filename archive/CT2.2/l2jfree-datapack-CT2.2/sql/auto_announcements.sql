-- ----------------------------------------
-- Table structure for auto_announcements
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `auto_announcements` (
  `id` INT UNSIGNED NOT NULL,
  `initial` BIGINT UNSIGNED NOT NULL,
  `delay` BIGINT UNSIGNED NOT NULL,
  `cycle` INT UNSIGNED NOT NULL,
  `memo` TEXT DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;
