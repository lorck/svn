-- ---------------------------
-- Table structure for `record`
-- ---------------------------
CREATE TABLE IF NOT EXISTS `record` ( 
  `maxplayer` int(5) NOT NULL DEFAULT '0', 
  `date` DATE NOT NULL 
) DEFAULT CHARSET=utf8;

INSERT INTO `record` VALUES (0, NOW());