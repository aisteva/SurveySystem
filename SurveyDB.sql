CREATE DATABASE  IF NOT EXISTS `SurveyDB` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_lithuanian_ci */;
USE `SurveyDB`;
-- MySQL dump 10.13  Distrib 5.6.24, for osx10.8 (x86_64)
--
-- Host: 193.219.91.103    Database: SurveyDB
-- ------------------------------------------------------
-- Server version	5.7.18

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `answer`
--

DROP TABLE IF EXISTS `answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `answer` (
  `AnswerID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `OfferedAnswerID` bigint(20) unsigned NOT NULL,
  `SessionID` varchar(15) CHARACTER SET utf8 COLLATE utf8_lithuanian_ci DEFAULT NULL,
  `Text` varchar(1000) CHARACTER SET utf8 COLLATE utf8_lithuanian_ci DEFAULT NULL,
  `isFinished` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`AnswerID`),
  UNIQUE KEY `AnswerID` (`AnswerID`),
  KEY `OfferedAnswerID` (`OfferedAnswerID`),
  CONSTRAINT `answer_ibfk_1` FOREIGN KEY (`OfferedAnswerID`) REFERENCES `offeredanswer` (`OfferedAnswerID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2500 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `answer`
--

LOCK TABLES `answer` WRITE;
/*!40000 ALTER TABLE `answer` DISABLE KEYS */;
/*!40000 ALTER TABLE `answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `answerconnection`
--

DROP TABLE IF EXISTS `answerconnection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `answerconnection` (
  `OfferedAnswerID` bigint(20) unsigned NOT NULL,
  `QuestionID` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`OfferedAnswerID`,`QuestionID`),
  KEY `OfferedAnswerID` (`OfferedAnswerID`,`QuestionID`),
  KEY `QuestionID` (`QuestionID`),
  CONSTRAINT `answerconnection_ibfk_1` FOREIGN KEY (`OfferedAnswerID`) REFERENCES `offeredanswer` (`OfferedAnswerID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `answerconnection_ibfk_2` FOREIGN KEY (`QuestionID`) REFERENCES `question` (`QuestionID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `answerconnection`
--

LOCK TABLES `answerconnection` WRITE;
/*!40000 ALTER TABLE `answerconnection` DISABLE KEYS */;
/*!40000 ALTER TABLE `answerconnection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `offeredanswer`
--

DROP TABLE IF EXISTS `offeredanswer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `offeredanswer` (
  `OfferedAnswerID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `QuestionID` bigint(20) unsigned NOT NULL,
  `Text` varchar(1000) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`OfferedAnswerID`),
  UNIQUE KEY `OfferedAnswerID` (`OfferedAnswerID`),
  KEY `QuestionID` (`QuestionID`),
  CONSTRAINT `offeredanswer_ibfk_1` FOREIGN KEY (`QuestionID`) REFERENCES `question` (`QuestionID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=862 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `offeredanswer`
--

LOCK TABLES `offeredanswer` WRITE;
/*!40000 ALTER TABLE `offeredanswer` DISABLE KEYS */;
/*!40000 ALTER TABLE `offeredanswer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person` (
  `PersonID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `opt_lock_version` int(11) NOT NULL,
  `FirstName` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `LastName` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `Email` varchar(255) CHARACTER SET utf8 NOT NULL,
  `Password` varchar(1000) CHARACTER SET utf8 DEFAULT NULL,
  `UserType` varchar(20) CHARACTER SET utf8 NOT NULL,
  `InviteExpiration` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isBlocked` tinyint(1) NOT NULL DEFAULT '0',
  `inviteURL` varchar(8) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`PersonID`),
  UNIQUE KEY `PersonID` (`PersonID`),
  UNIQUE KEY `PersonEmail` (`Email`),
  UNIQUE KEY `PersonInviteUrl` (`inviteURL`)
) ENGINE=InnoDB AUTO_INCREMENT=213 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person`
--

LOCK TABLES `person` WRITE;
/*!40000 ALTER TABLE `person` DISABLE KEYS */;
INSERT INTO `person` VALUES (212,2,'Admin','Admin','admin','e0pCKRT6DPc+fH8HGN3xDxzDd+iGcCePhguEUJhBlQuFn174z1U5kz4chhgFTBDujOW2d8cXEtvf\nv2RH3XctwQ==','ADMIN','2017-05-30 22:01:56',0,NULL);
/*!40000 ALTER TABLE `person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `question` (
  `QuestionID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `SurveyID` bigint(20) unsigned NOT NULL,
  `QuestionText` varchar(1000) CHARACTER SET utf8 NOT NULL,
  `QuestionNumber` int(11) NOT NULL,
  `Page` int(11) DEFAULT NULL,
  `Type` varchar(20) CHARACTER SET utf8 NOT NULL,
  `isRequired` tinyint(1) NOT NULL,
  PRIMARY KEY (`QuestionID`),
  UNIQUE KEY `QuestionID` (`QuestionID`),
  KEY `SurveyID` (`SurveyID`),
  CONSTRAINT `question_ibfk_1` FOREIGN KEY (`SurveyID`) REFERENCES `survey` (`SurveyID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=551 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question`
--

LOCK TABLES `question` WRITE;
/*!40000 ALTER TABLE `question` DISABLE KEYS */;
/*!40000 ALTER TABLE `question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `survey`
--

DROP TABLE IF EXISTS `survey`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `survey` (
  `SurveyID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `opt_lock_version` int(11) NOT NULL,
  `Title` varchar(1000) CHARACTER SET utf8 DEFAULT 'Be pavadinimo',
  `PersonID` bigint(20) unsigned NOT NULL,
  `Description` varchar(1000) CHARACTER SET utf8 COLLATE utf8_lithuanian_ci DEFAULT NULL,
  `StartDate` date NOT NULL,
  `EndDate` date DEFAULT NULL,
  `SurveyURL` varchar(30) NOT NULL,
  `isOpen` tinyint(1) NOT NULL,
  `isCreated` tinyint(1) NOT NULL,
  `isPrivate` tinyint(1) NOT NULL,
  `submits` int(11) DEFAULT NULL,
  PRIMARY KEY (`SurveyID`),
  UNIQUE KEY `SurveyID` (`SurveyID`),
  UNIQUE KEY `survey_SurveyURL_uindex` (`SurveyURL`),
  KEY `PersonID` (`PersonID`),
  CONSTRAINT `survey_ibfk_1` FOREIGN KEY (`PersonID`) REFERENCES `person` (`PersonID`)
) ENGINE=InnoDB AUTO_INCREMENT=804 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `survey`
--

LOCK TABLES `survey` WRITE;
/*!40000 ALTER TABLE `survey` DISABLE KEYS */;
/*!40000 ALTER TABLE `survey` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-05-31  1:03:11
