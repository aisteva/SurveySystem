-- phpMyAdmin SQL Dump
-- version 4.6.4
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 02, 2017 at 12:31 PM
-- Server version: 5.7.14
-- PHP Version: 5.6.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `surveydb`
--

-- --------------------------------------------------------

--
-- Table structure for table `answer`
--

CREATE TABLE `answer` (
  `AnswerID` bigint(20) UNSIGNED NOT NULL,
  `OfferedAnswerID` bigint(20) UNSIGNED NOT NULL,
  `SessionID` int(11) NOT NULL,
  `Text` varchar(1000) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `answerconnection`
--

CREATE TABLE `answerconnection` (
  `AnswerConnectionID` bigint(20) UNSIGNED NOT NULL,
  `OfferedAnswerID` bigint(20) UNSIGNED NOT NULL,
  `QuestionID` bigint(20) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `offeredanswer`
--

CREATE TABLE `offeredanswer` (
  `OfferedAnswerID` bigint(20) UNSIGNED NOT NULL,
  `QuestionID` bigint(20) UNSIGNED NOT NULL,
  `Text` varchar(1000) CHARACTER SET utf8 NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `person`
--

CREATE TABLE `person` (
  `PersonID` bigint(20) UNSIGNED NOT NULL,
  `opt_lock_version` int(11) NOT NULL,
  `FirstName` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `LastName` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `Email` varchar(255) CHARACTER SET utf8 NOT NULL,
  `Password` varchar(1000) CHARACTER SET utf8 DEFAULT NULL,
  `UserType` varchar(20) CHARACTER SET utf8 NOT NULL,
  `InviteExpiration` timestamp DEFAULT CURRENT_TIMESTAMP,
  `isBlocked` tinyint(1) NOT NULL DEFAULT '0',
  `inviteURL` varchar(8) CHARACTER SET utf8 DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `question`
--

CREATE TABLE `question` (
  `QuestionID` bigint(20) UNSIGNED NOT NULL,
  `SurveyID` bigint(20) UNSIGNED NOT NULL,
  `QuestionText` varchar(1000) CHARACTER SET utf8 NOT NULL,
  `QuestionNumber` int(11) NOT NULL,
  `Page` int(11) DEFAULT NULL,
  `Type` varchar(20) CHARACTER SET utf8 NOT NULL,
  `isRequired` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `survey`
--

CREATE TABLE `survey` (
  `SurveyID` bigint(20) UNSIGNED NOT NULL,
  `PersonID` bigint(20) UNSIGNED NOT NULL,
  `Description` varchar(1000) CHARACTER SET utf8 DEFAULT NULL,
  `StartDate` date NOT NULL,
  `EndDate` date DEFAULT NULL,
  `SurveyURL` varchar(8) CHARACTER SET utf8 NOT NULL,
  `isOpen` tinyint(1) NOT NULL,
  `isCreated` tinyint(1) NOT NULL,
  `isPrivate` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `answer`
--
ALTER TABLE `answer`
  ADD PRIMARY KEY (`AnswerID`),
  ADD UNIQUE KEY `AnswerID` (`AnswerID`),
  ADD KEY `OfferedAnswerID` (`OfferedAnswerID`);

--
-- Indexes for table `answerconnection`
--
ALTER TABLE `answerconnection`
  ADD PRIMARY KEY (`AnswerConnectionID`),
  ADD UNIQUE KEY `AnswerConnectionID` (`AnswerConnectionID`),
  ADD KEY `OfferedAnswerID` (`OfferedAnswerID`,`QuestionID`),
  ADD KEY `QuestionID` (`QuestionID`);

--
-- Indexes for table `offeredanswer`
--
ALTER TABLE `offeredanswer`
  ADD PRIMARY KEY (`OfferedAnswerID`),
  ADD UNIQUE KEY `OfferedAnswerID` (`OfferedAnswerID`),
  ADD KEY `QuestionID` (`QuestionID`);

--
-- Indexes for table `person`
--
ALTER TABLE `person`
  ADD PRIMARY KEY (`PersonID`),
  ADD UNIQUE KEY `PersonID` (`PersonID`),
  ADD UNIQUE KEY `PersonInviteUrl` (`inviteURL`),
  ADD UNIQUE KEY `PersonEmail` (`Email`);

--
-- Indexes for table `question`
--
ALTER TABLE `question`
  ADD PRIMARY KEY (`QuestionID`),
  ADD UNIQUE KEY `QuestionID` (`QuestionID`),
  ADD KEY `SurveyID` (`SurveyID`);

--
-- Indexes for table `survey`
--
ALTER TABLE `survey`
  ADD PRIMARY KEY (`SurveyID`),
  ADD UNIQUE KEY `SurveyID` (`SurveyID`),
  ADD KEY `PersonID` (`PersonID`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `answer`
--
ALTER TABLE `answer`
  MODIFY `AnswerID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `answerconnection`
--
ALTER TABLE `answerconnection`
  MODIFY `AnswerConnectionID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `offeredanswer`
--
ALTER TABLE `offeredanswer`
  MODIFY `OfferedAnswerID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `person`
--
ALTER TABLE `person`
  MODIFY `PersonID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `question`
--
ALTER TABLE `question`
  MODIFY `QuestionID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `survey`
--
ALTER TABLE `survey`
  MODIFY `SurveyID` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `answer`
--
ALTER TABLE `answer`
  ADD CONSTRAINT `answer_ibfk_1` FOREIGN KEY (`OfferedAnswerID`) REFERENCES `offeredanswer` (`OfferedAnswerID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `answerconnection`
--
ALTER TABLE `answerconnection`
  ADD CONSTRAINT `answerconnection_ibfk_1` FOREIGN KEY (`OfferedAnswerID`) REFERENCES `offeredanswer` (`OfferedAnswerID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `answerconnection_ibfk_2` FOREIGN KEY (`QuestionID`) REFERENCES `question` (`QuestionID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `offeredanswer`
--
ALTER TABLE `offeredanswer`
  ADD CONSTRAINT `offeredanswer_ibfk_1` FOREIGN KEY (`QuestionID`) REFERENCES `question` (`QuestionID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `question_ibfk_1` FOREIGN KEY (`SurveyID`) REFERENCES `survey` (`SurveyID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `survey`
--
ALTER TABLE `survey`
  ADD CONSTRAINT `survey_ibfk_1` FOREIGN KEY (`PersonID`) REFERENCES `person` (`PersonID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
