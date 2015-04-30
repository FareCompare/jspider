--
-- JSpider database creation script
--
-- WARNING: This JDBC storage provider is an alpha release.
-- It has only been tested on MySQL 3.
--
-- $Id: jspiderdb.sql,v 1.6 2003/04/09 17:08:02 vanrogu Exp $
--

--
-- MySQL dump 8.22
--
-- Host: localhost    Database: jspider
-- -------------------------------------------------------
-- Server version	3.23.55-nt

--
-- Table structure for table 'jspider_content'
--
CREATE DATABASE IF NOT EXISTS seo_spider;

USE seo_spider;


DROP TABLE IF EXISTS jspider_content;
DROP TABLE IF EXISTS jspider_site;
DROP TABLE IF EXISTS jspider_cookie;
DROP TABLE IF EXISTS jspider_decision;
DROP TABLE IF EXISTS jspider_decision_step;
DROP TABLE IF EXISTS jspider_email_address;
DROP TABLE IF EXISTS jspider_email_address_reference;
DROP TABLE IF EXISTS jspider_folder;
DROP TABLE IF EXISTS jspider_resource;
DROP TABLE IF EXISTS jspider_resource_reference;


CREATE TABLE IF NOT EXISTS jspider_content (
    id      INT(11) NOT NULL DEFAULT '0',
    content BLOB
)
    ENGINE =InnoDB;

--
-- Table structure for table 'jspider_cookie'
--

CREATE TABLE IF NOT EXISTS jspider_cookie (
    id      INT(11)      NOT NULL AUTO_INCREMENT,
    site    INT(11)      NOT NULL DEFAULT '0',
    name    VARCHAR(255) NOT NULL DEFAULT '',
    value   VARCHAR(255) NOT NULL DEFAULT '',
    domain  VARCHAR(255) NOT NULL DEFAULT '',
    path    VARCHAR(255) NOT NULL DEFAULT '',
    expires VARCHAR(255)          DEFAULT NULL,
    PRIMARY KEY (id)
)
    ENGINE =InnoDB;

--
-- Table structure for table 'jspider_decision'
--

CREATE TABLE IF NOT EXISTS jspider_decision (
    resource INT(11)  NOT NULL DEFAULT '0',
    subject  INT(11)  NOT NULL DEFAULT '0',
    type     INT(11)  NOT NULL DEFAULT '0',
    comment  LONGTEXT NOT NULL,
    PRIMARY KEY (resource, subject)
)
    ENGINE =InnoDB;

--
-- Table structure for table 'jspider_decision_step'
--

CREATE TABLE IF NOT EXISTS jspider_decision_step (
    resource INT(11)  NOT NULL DEFAULT '0',
    subject  INT(11)  NOT NULL DEFAULT '0',
    sequence INT(11)  NOT NULL DEFAULT '0',
    type     INT(11)  NOT NULL DEFAULT '0',
    rule     LONGTEXT NOT NULL,
    decision INT(11)  NOT NULL DEFAULT '0',
    comment  LONGTEXT NOT NULL,
    PRIMARY KEY (resource, subject, sequence)
)
    ENGINE =InnoDB;

--
-- Table structure for table 'jspider_email_address'
--

CREATE TABLE IF NOT EXISTS jspider_email_address (
    id      INT(11)      NOT NULL AUTO_INCREMENT,
    address VARCHAR(255) NOT NULL DEFAULT '',
    PRIMARY KEY (id)
)
    ENGINE =InnoDB;

--
-- Table structure for table 'jspider_email_address_reference'
--

CREATE TABLE IF NOT EXISTS jspider_email_address_reference (
    resource INT(11) NOT NULL DEFAULT '0',
    address  INT(11) NOT NULL DEFAULT '0',
    count    INT(11) NOT NULL DEFAULT '0',
    PRIMARY KEY (resource, address)
)
    ENGINE =InnoDB;

--
-- Table structure for table 'jspider_folder'
--

CREATE TABLE IF NOT EXISTS jspider_folder (
    id     INT(11)  NOT NULL AUTO_INCREMENT,
    parent INT(11)  NOT NULL DEFAULT '0',
    site   INT(11)  NOT NULL DEFAULT '0',
    name   LONGTEXT NOT NULL,
    PRIMARY KEY (id)
)
    ENGINE =InnoDB;

--
-- Table structure for table 'jspider_resource'
--

CREATE TABLE IF NOT EXISTS jspider_resource (
    id         INT(11)  NOT NULL AUTO_INCREMENT,
    url        LONGTEXT NOT NULL,
    state      INT(11)  NOT NULL DEFAULT '0',
    httpstatus INT(11)  NOT NULL DEFAULT '0',
    site       INT(11)  NOT NULL DEFAULT '0',
    timems     INT(11)  NOT NULL DEFAULT '0',
    mimetype   VARCHAR(255)      DEFAULT NULL,
    size       INT(11)  NOT NULL DEFAULT '0',
    folder     INT(11)  NOT NULL DEFAULT '0',
    PRIMARY KEY (id),
    KEY siteIdx (site),
    KEY folderIdx (folder)
)
    ENGINE =InnoDB;

--
-- Table structure for table 'jspider_resource_reference'
--

CREATE TABLE IF NOT EXISTS jspider_resource_reference (
    referer INT(11) NOT NULL DEFAULT '0',
    referee INT(11) NOT NULL DEFAULT '0',
    count   INT(11) NOT NULL DEFAULT '0',
    PRIMARY KEY (referer, referee)
)
    ENGINE =InnoDB;

--
-- Table structure for table 'jspider_site'
--

CREATE TABLE IF NOT EXISTS jspider_site (
    id               INT(11)      NOT NULL AUTO_INCREMENT,
    host             VARCHAR(255) NOT NULL DEFAULT '',
    port             INT(11)      NOT NULL DEFAULT '80',
    robotstxthandled TINYINT(4)   NOT NULL DEFAULT '0',
    usecookies       TINYINT(4)   NOT NULL DEFAULT '0',
    useproxy         TINYINT(4)   NOT NULL DEFAULT '0',
    state            INT(11)      NOT NULL DEFAULT '0',
    obeyrobotstxt    INT(11)      NOT NULL DEFAULT '0',
    fetchrobotstxt   INT(11)      NOT NULL DEFAULT '0',
    basesite         INT(11)      NOT NULL DEFAULT '0',
    useragent        VARCHAR(255) NOT NULL DEFAULT '',
    handle           INT(11)      NOT NULL DEFAULT '0',
    PRIMARY KEY (id)
)
    ENGINE =InnoDB;
