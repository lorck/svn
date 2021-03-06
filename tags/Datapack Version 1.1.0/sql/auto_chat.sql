-- ---------------------------
-- Table structure for `auto_chat`
-- ---------------------------
DROP TABLE IF EXISTS `auto_chat`;
CREATE TABLE `auto_chat` (
  `groupId` int(11) NOT NULL DEFAULT '0',
  `groupName` varchar(128) NOT NULL,
  `npcId` int(11) NOT NULL DEFAULT '0',
  `chatDelay` bigint(20) NOT NULL DEFAULT '-1',
  `chatRange` smallint(6) NOT NULL DEFAULT '-1',
  `chatRandom` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`groupId`)
) DEFAULT CHARSET=utf8;

INSERT INTO `auto_chat` VALUES 

-- Preacher of Doom
(1,'',31093,-1,-1,0),
(2,'',31172,-1,-1,0),
(3,'',31174,-1,-1,0),
(4,'',31176,-1,-1,0),
(5,'',31178,-1,-1,0),
(6,'',31180,-1,-1,0),
(7,'',31182,-1,-1,0),
(8,'',31184,-1,-1,0),
(9,'',31186,-1,-1,0),
(10,'',31188,-1,-1,0),
(11,'',31190,-1,-1,0),
(12,'',31192,-1,-1,0),
(13,'',31194,-1,-1,0),
(14,'',31196,-1,-1,0),
(15,'',31198,-1,-1,0),
(16,'',31200,-1,-1,0),

-- Orator of Revelations
(17,'',31094,-1,-1,0),
(18,'',31173,-1,-1,0),
(19,'',31175,-1,-1,0),
(20,'',31177,-1,-1,0),
(21,'',31179,-1,-1,0),
(22,'',31181,-1,-1,0),
(23,'',31183,-1,-1,0),
(24,'',31185,-1,-1,0),
(25,'',31187,-1,-1,0),
(26,'',31189,-1,-1,0),
(27,'',31191,-1,-1,0),
(28,'',31193,-1,-1,0),
(29,'',31195,-1,-1,0),
(30,'',31197,-1,-1,0),
(31,'',31199,-1,-1,0),
(32,'',31201,-1,-1,0);