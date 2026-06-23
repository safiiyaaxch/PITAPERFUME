-- MySQL dump 10.13  Distrib 8.0.45, for Linux (x86_64)
--
-- Host: localhost    Database: scentify_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `cartItemId` bigint NOT NULL AUTO_INCREMENT,
  `added_at` datetime(6) DEFAULT NULL,
  `quantity` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `cartId` bigint NOT NULL,
  `productId` char(3) NOT NULL,
  PRIMARY KEY (`cartItemId`),
  KEY `FKmdxq4wqg8j7ojn75ne7uhfghj` (`cartId`),
  KEY `FKld7k7jxuo00jnnjws80q6u3qn` (`productId`),
  CONSTRAINT `FKld7k7jxuo00jnnjws80q6u3qn` FOREIGN KEY (`productId`) REFERENCES `product` (`productId`),
  CONSTRAINT `FKmdxq4wqg8j7ojn75ne7uhfghj` FOREIGN KEY (`cartId`) REFERENCES `shopping_cart` (`cartId`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customerid` int NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `country` varchar(50) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `fullname` varchar(100) NOT NULL,
  `is_member` bit(1) DEFAULT NULL,
  `loyalty_points` int DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `preferred_scent_type` varchar(100) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `userid` int NOT NULL,
  PRIMARY KEY (`customerid`),
  UNIQUE KEY `UK7fs7b2coc7qru1qcvy91erql4` (`userid`),
  CONSTRAINT `FKm6c7m2yovk9ppw34vo8huj79n` FOREIGN KEY (`userid`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,NULL,NULL,NULL,'2026-04-15 15:00:26.857695','Haziqah',_binary '',0,'01128940388',NULL,'2026-05-02 09:32:40.137175',1),(2,'123 Member Street','Kuala Lumpur','Malaysia','2026-04-21 07:28:52.000000','John Member',_binary '',500,'0123456789','Floral','2026-04-21 07:28:52.000000',4),(3,'456 Regular Avenue','Selangor','Malaysia','2026-04-21 07:28:52.000000','Jane Non-Member',_binary '\0',0,'9876543210','Oriental','2026-04-21 07:28:52.000000',5),(4,NULL,NULL,NULL,'2026-06-03 02:46:00.198234','Najwa Safiya',_binary '',0,NULL,NULL,'2026-06-04 02:05:14.547718',6),(5,NULL,NULL,NULL,'2026-06-10 16:45:33.604634','Aqilah',_binary '',0,NULL,NULL,'2026-06-22 08:16:47.965332',8);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoices`
--

DROP TABLE IF EXISTS `invoices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoices` (
  `invoiceId` bigint NOT NULL AUTO_INCREMENT,
  `invoiceDate` datetime(6) NOT NULL,
  `invoiceHtml` text,
  `invoiceNumber` varchar(50) NOT NULL,
  `paymentMethod` varchar(50) DEFAULT NULL,
  `paymentReference` varchar(100) DEFAULT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `tax` decimal(10,2) NOT NULL,
  `taxRate` varchar(20) DEFAULT NULL,
  `total` decimal(10,2) NOT NULL,
  `orderId` bigint NOT NULL,
  `paymentId` bigint DEFAULT NULL,
  PRIMARY KEY (`invoiceId`),
  UNIQUE KEY `UKgwqud8ggt742y8g83ke44qvx` (`invoiceNumber`),
  UNIQUE KEY `UKgqpnmmc9qr1sb2nfsk9a1amco` (`orderId`),
  UNIQUE KEY `UKmuwlejxj5j0vxsunff568k306` (`paymentId`),
  CONSTRAINT `FKkwv3mbtfs7s4t1fucjnl5k2w9` FOREIGN KEY (`paymentId`) REFERENCES `payments` (`paymentId`),
  CONSTRAINT `FKx74cvqt0f0n8ptskn44fsrmu` FOREIGN KEY (`orderId`) REFERENCES `orders` (`orderId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoices`
--

LOCK TABLES `invoices` WRITE;
/*!40000 ALTER TABLE `invoices` DISABLE KEYS */;
INSERT INTO `invoices` VALUES (1,'2026-06-18 10:06:21.888333',NULL,'INV-20260618-7635','ToyyibPay','ebok316q',850.00,0.00,'0%',850.00,51,14),(2,'2026-06-18 11:02:59.969039',NULL,'INV-20260618-7882','ToyyibPay','boab882y',279.00,0.00,'0%',279.00,54,17),(3,'2026-06-22 07:57:35.890611',NULL,'INV-20260622-4100','ToyyibPay','ys1qv1r8',148.00,0.00,'0%',148.00,52,15),(4,'2026-06-22 13:23:35.410043',NULL,'INV-20260622-1999','ToyyibPay','hhxf81fy',578.00,0.00,'0%',578.00,53,16);
/*!40000 ALTER TABLE `invoices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `membership_application`
--

DROP TABLE IF EXISTS `membership_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `membership_application` (
  `application_id` int NOT NULL AUTO_INCREMENT,
  `applied_date` datetime(6) NOT NULL,
  `approved_date` datetime(6) DEFAULT NULL,
  `notes` varchar(500) DEFAULT NULL,
  `rejection_reason` varchar(500) DEFAULT NULL,
  `application_status` varchar(20) NOT NULL,
  `terms_accepted` bit(1) NOT NULL,
  `customer_id` int NOT NULL,
  PRIMARY KEY (`application_id`),
  UNIQUE KEY `UKetxj6iqtr1lmtqvwiios6d47u` (`customer_id`),
  CONSTRAINT `FKkjabmuvq7xpb6onl7p9aywg6w` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customerid`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `membership_application`
--

LOCK TABLES `membership_application` WRITE;
/*!40000 ALTER TABLE `membership_application` DISABLE KEYS */;
INSERT INTO `membership_application` VALUES (1,'2026-05-02 08:55:46.300436','2026-05-02 09:32:40.080577',NULL,NULL,'APPROVED',_binary '',1),(2,'2026-06-04 02:04:20.547981','2026-06-04 02:05:14.505609',NULL,NULL,'APPROVED',_binary '',4),(3,'2026-06-22 08:16:18.673186','2026-06-22 08:16:47.881390',NULL,NULL,'APPROVED',_binary '',5);
/*!40000 ALTER TABLE `membership_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `orderItemId` bigint NOT NULL AUTO_INCREMENT,
  `price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `orderId` bigint NOT NULL,
  `productId` varchar(255) NOT NULL,
  PRIMARY KEY (`orderItemId`),
  KEY `FK5dledqxrq55xmpqy9fr4cpbsu` (`orderId`),
  KEY `FKspfucjf8lis7m67anq798w566` (`productId`),
  CONSTRAINT `FK5dledqxrq55xmpqy9fr4cpbsu` FOREIGN KEY (`orderId`) REFERENCES `orders` (`orderId`),
  CONSTRAINT `FKspfucjf8lis7m67anq798w566` FOREIGN KEY (`productId`) REFERENCES `product` (`productId`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (12,850.00,1,850.00,49,'P4'),(13,850.00,1,850.00,50,'P4'),(14,850.00,1,850.00,51,'P4'),(15,148.00,1,148.00,52,'P13'),(16,450.00,1,450.00,53,'P5'),(17,128.00,1,128.00,53,'P12'),(18,149.00,1,149.00,54,'P10'),(19,130.00,1,130.00,54,'P11'),(20,625.00,1,625.00,55,'P7'),(21,130.00,1,130.00,56,'P11'),(22,1200.00,1,1200.00,57,'P2'),(23,850.00,1,850.00,58,'P6'),(24,850.00,1,850.00,59,'P6'),(25,850.00,1,850.00,60,'P6');
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_voucher`
--

DROP TABLE IF EXISTS `order_voucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_voucher` (
  `order_voucher_id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `customer_id` int NOT NULL,
  `discount_amount` decimal(38,2) NOT NULL,
  `order_id` int NOT NULL,
  `used_date` datetime(6) DEFAULT NULL,
  `voucher_id` int NOT NULL,
  PRIMARY KEY (`order_voucher_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_voucher`
--

LOCK TABLES `order_voucher` WRITE;
/*!40000 ALTER TABLE `order_voucher` DISABLE KEYS */;
INSERT INTO `order_voucher` VALUES (1,'2026-05-03 07:30:50.602734',2,360.00,13,'2026-05-03 07:30:50.601145',1),(2,'2026-05-06 14:23:02.520034',2,360.00,23,'2026-05-06 14:23:02.515591',1),(3,'2026-05-06 14:24:26.189228',2,360.00,24,'2026-05-06 14:24:26.185865',1),(4,'2026-05-06 15:09:23.947700',2,360.00,28,'2026-05-06 15:09:23.943212',1),(5,'2026-06-04 01:26:15.077756',1,30.00,30,'2026-06-04 01:26:15.074700',1),(6,'2026-06-04 01:42:34.603287',1,30.00,33,'2026-06-04 01:42:34.598049',1),(7,'2026-06-04 02:07:31.668838',1,20.00,34,'2026-06-04 02:07:31.665892',4);
/*!40000 ALTER TABLE `order_voucher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `orderId` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `delivery_date` datetime(6) DEFAULT NULL,
  `order_date` datetime(6) DEFAULT NULL,
  `orderStatus` varchar(50) DEFAULT NULL,
  `shippingAddress` varchar(500) DEFAULT NULL,
  `totalPrice` decimal(10,2) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `customerId` int NOT NULL,
  `paymentStatus` varchar(50) DEFAULT NULL,
  `toyyibPayBillCode` varchar(100) DEFAULT NULL,
  `discountAmount` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`orderId`),
  KEY `FK1ptw1l1kw6lk5o8mve55enocc` (`customerId`),
  CONSTRAINT `FK1ptw1l1kw6lk5o8mve55enocc` FOREIGN KEY (`customerId`) REFERENCES `customer` (`customerid`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'2026-04-20 14:38:11.130745',NULL,'2026-04-20 14:38:11.130745','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 14:38:11.130745',1,'PENDING',NULL,NULL),(2,'2026-04-20 14:39:02.250388',NULL,'2026-04-20 14:39:02.250388','PENDING','Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 14:39:02.250388',1,'PENDING',NULL,NULL),(3,'2026-04-20 14:40:26.282993',NULL,'2026-04-20 14:40:26.282993','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 14:40:26.282993',1,'PENDING',NULL,NULL),(4,'2026-04-20 15:09:36.770699',NULL,'2026-04-20 15:09:36.770699','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 15:09:36.770699',1,'PENDING',NULL,NULL),(5,'2026-04-20 15:15:34.001775',NULL,'2026-04-20 15:15:34.001775','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 15:15:34.001775',1,'PENDING',NULL,NULL),(6,'2026-04-20 15:17:42.975996',NULL,'2026-04-20 15:17:42.975996','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 15:17:42.975996',1,'PENDING',NULL,NULL),(7,'2026-04-20 15:26:08.363604',NULL,'2026-04-20 15:26:08.363604','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 15:26:08.363604',1,'PENDING',NULL,NULL),(8,'2026-04-20 15:26:42.708982',NULL,'2026-04-20 15:26:42.708659','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 15:26:42.708982',1,'PENDING',NULL,NULL),(9,'2026-04-20 15:27:08.084962',NULL,'2026-04-20 15:27:08.084962','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 15:27:08.084962',1,'PENDING',NULL,NULL),(10,'2026-04-20 15:30:20.590872',NULL,'2026-04-20 15:30:20.590872','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 15:30:20.590872',1,'PENDING',NULL,NULL),(11,'2026-04-20 15:33:44.447709',NULL,'2026-04-20 15:33:44.447144','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 15:33:44.447709',1,'PENDING',NULL,NULL),(12,'2026-04-20 15:39:29.970531',NULL,'2026-04-20 15:39:29.970531','PENDING','Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-04-20 15:39:29.970531',1,'PENDING',NULL,NULL),(13,'2026-05-03 07:30:50.538471',NULL,'2026-05-03 07:30:50.538471','PENDING','123 Member Street, Kuala Lumpur, Malaysia',844.90,'2026-05-03 07:30:50.538471',2,'PENDING',NULL,NULL),(14,'2026-05-03 07:31:01.654250',NULL,'2026-05-03 07:31:01.654250','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-03 07:31:01.654250',2,'PENDING',NULL,NULL),(15,'2026-05-03 08:18:12.738557',NULL,'2026-05-03 08:18:12.738557','PENDING','150, Laluan Raia Savanna 8, Taman Raia Savanna, Batu Gajah, Malaysia',1204.90,'2026-05-03 08:18:12.738557',1,'PENDING',NULL,NULL),(16,'2026-05-03 08:18:50.837690',NULL,'2026-05-03 08:18:50.837690','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',1204.90,'2026-05-03 08:18:50.837690',1,'PENDING',NULL,NULL),(17,'2026-05-03 08:24:37.042478',NULL,'2026-05-03 08:24:37.042478','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-03 08:24:37.042478',2,'PENDING',NULL,NULL),(18,'2026-05-03 08:29:05.292985',NULL,'2026-05-03 08:29:05.292985','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-03 08:29:05.292985',2,'PENDING',NULL,NULL),(19,'2026-05-03 08:37:16.204948',NULL,'2026-05-03 08:37:16.204948','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-03 08:37:16.204948',2,'PENDING',NULL,NULL),(20,'2026-05-03 08:37:33.218822',NULL,'2026-05-03 08:37:33.218822','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-03 08:37:33.218822',2,'PENDING',NULL,NULL),(21,'2026-05-03 08:39:56.112002',NULL,'2026-05-03 08:39:56.112002','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-03 08:39:56.112002',2,'PENDING',NULL,NULL),(22,'2026-05-03 08:40:07.546147',NULL,'2026-05-03 08:40:07.546147','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-03 08:40:07.546147',2,'PENDING',NULL,NULL),(23,'2026-05-06 14:23:02.352862',NULL,'2026-05-06 14:23:02.352862','PENDING','123 Member Street, Kuala Lumpur, Malaysia',844.90,'2026-05-06 14:23:02.352862',2,'PENDING','u8qok0jx',NULL),(24,'2026-05-06 14:24:26.104148',NULL,'2026-05-06 14:24:26.104148','PENDING','123 Member Street, Kuala Lumpur, Malaysia',844.90,'2026-05-06 14:24:26.104148',2,'PENDING','lmnhxw22',NULL),(25,'2026-05-06 14:27:55.019578',NULL,'2026-05-06 14:27:55.019578','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-06 14:27:55.019578',2,'PENDING','6nq1r21h',NULL),(26,'2026-05-06 14:31:47.032069',NULL,'2026-05-06 14:31:47.031422','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-06 14:31:47.032069',2,'PENDING','lpwqsdby',NULL),(27,'2026-05-06 14:35:01.103839',NULL,'2026-05-06 14:35:01.103839','PENDING','123 Member Street, Kuala Lumpur, Malaysia',1204.90,'2026-05-06 14:35:01.103839',2,'PENDING','q3lb6pfl',NULL),(28,'2026-05-06 15:09:23.840186',NULL,'2026-05-06 15:09:23.840186','CONFIRMED','123 Member Street, Kuala Lumpur, Malaysia',844.90,'2026-05-06 15:09:23.840186',2,'PAID','407fk5f4',NULL),(29,'2026-06-03 01:03:40.326897',NULL,'2026-06-03 01:03:40.326897','CONFIRMED','150, Batu Gajah, Malaysia',2104.90,'2026-06-03 01:03:40.326897',1,'PAID','wvr9kcf3',NULL),(30,'2026-06-04 01:26:15.001314',NULL,'2026-06-04 01:26:15.001314','CONFIRMED','Laluan Raia, Kuala Lumpur, Malaysia',824.90,'2026-06-04 01:26:15.001314',1,'PAID','fqcr79ma',NULL),(33,'2026-06-04 01:42:34.494205',NULL,'2026-06-04 01:42:34.494205','PENDING','10, Laluan Raja, Kuala Lumpur, Malaysia',1274.90,'2026-06-04 01:42:34.494205',1,'PENDING','wwl9ozqr',NULL),(34,'2026-06-04 02:07:31.597349',NULL,'2026-06-04 02:07:31.597349','CONFIRMED','10 Laluan Raia, Batu Gajah, Malaysia',1284.90,'2026-06-04 02:07:31.597349',1,'PAID','wlmmb72a',NULL),(35,'2026-06-10 16:37:41.357297',NULL,'2026-06-10 16:37:41.357297','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',854.90,'2026-06-10 16:37:41.357297',1,'PENDING','lvnw0j9j',NULL),(36,'2026-06-11 03:28:33.807636',NULL,'2026-06-11 03:28:33.807636','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',854.90,'2026-06-11 03:28:33.807636',1,'PENDING','kbyq1odz',NULL),(37,'2026-06-11 03:30:16.318654',NULL,'2026-06-11 03:30:16.318654','CONFIRMED','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',854.90,'2026-06-11 03:30:16.318654',1,'PAID','kcr4k5az',NULL),(39,'2026-06-17 17:17:31.219417',NULL,'2026-06-17 17:17:31.219417','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',1700.00,'2026-06-17 17:17:31.219417',1,'PENDING',NULL,NULL),(40,'2026-06-17 17:21:29.787798',NULL,'2026-06-17 17:21:29.787798','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',1700.00,'2026-06-17 17:21:29.787798',1,'PENDING',NULL,NULL),(41,'2026-06-17 17:25:47.878731',NULL,'2026-06-17 17:25:47.878731','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',1700.00,'2026-06-17 17:25:47.878731',1,'PENDING',NULL,NULL),(42,'2026-06-17 17:38:54.678729',NULL,'2026-06-17 17:38:54.678729','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',1700.00,'2026-06-17 17:38:54.678729',1,'PENDING',NULL,NULL),(43,'2026-06-17 17:49:37.981618',NULL,'2026-06-17 17:49:37.981618','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',1700.00,'2026-06-17 17:49:37.981618',1,'PENDING',NULL,NULL),(44,'2026-06-17 17:49:55.058104',NULL,'2026-06-17 17:49:55.058104','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',1700.00,'2026-06-17 17:49:55.058104',1,'PENDING',NULL,NULL),(45,'2026-06-17 17:50:20.620883',NULL,'2026-06-17 17:50:20.620883','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-17 17:50:20.620883',1,'PENDING',NULL,NULL),(46,'2026-06-18 04:33:08.022322',NULL,'2026-06-18 04:33:08.022322','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-18 04:33:08.022322',1,'PENDING',NULL,NULL),(47,'2026-06-18 04:33:50.332408',NULL,'2026-06-18 04:33:50.332408','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-18 04:33:50.332408',1,'PENDING',NULL,NULL),(48,'2026-06-18 05:04:29.104130',NULL,'2026-06-18 05:04:29.104130','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-18 05:04:29.104130',1,'PENDING',NULL,NULL),(49,'2026-06-18 05:26:43.281723',NULL,'2026-06-18 05:26:43.281723','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-18 05:26:43.281723',1,'PENDING',NULL,NULL),(50,'2026-06-18 05:27:07.995948',NULL,'2026-06-18 05:27:07.995948','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-18 05:27:07.995948',1,'PENDING',NULL,NULL),(51,'2026-06-18 05:31:11.491965','2026-06-18 05:32:06.428659','2026-06-18 05:31:11.491965','DELIVERED','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-18 05:32:06.428659',1,'PAID','ebok316q',NULL),(52,'2026-06-18 05:45:15.573392','2026-06-18 05:45:44.403677','2026-06-18 05:45:15.573392','DELIVERED','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',148.00,'2026-06-18 05:45:44.403677',1,'PAID','ys1qv1r8',NULL),(53,'2026-06-18 05:47:47.340020','2026-06-18 05:48:12.251700','2026-06-18 05:47:47.340020','DELIVERED','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',578.00,'2026-06-18 05:48:12.251700',1,'PAID','hhxf81fy',NULL),(54,'2026-06-18 11:01:59.342634','2026-06-18 11:02:59.916806','2026-06-18 11:01:59.342634','DELIVERED','123 Member Street, Kuala Lumpur, Malaysia',279.00,'2026-06-18 11:02:59.916806',2,'PAID','boab882y',NULL),(55,'2026-06-18 11:04:27.458443',NULL,'2026-06-18 11:04:27.458443','CANCELLED','123 Member Street, Kuala Lumpur, Malaysia',625.00,'2026-06-18 11:04:27.458443',2,'FAILED','0e1kvjox',NULL),(56,'2026-06-18 11:06:47.337478',NULL,'2026-06-18 11:06:47.337478','PENDING','123 Member Street, Kuala Lumpur, Malaysia',130.00,'2026-06-18 11:06:47.337478',2,'PENDING','se3skdzt',NULL),(57,'2026-06-18 11:08:00.236094',NULL,'2026-06-18 11:08:00.236094','CANCELLED','123 Member Street, Kuala Lumpur, Malaysia',1200.00,'2026-06-18 11:08:00.236094',2,'FAILED','563v2t58',NULL),(58,'2026-06-18 11:30:07.244414',NULL,'2026-06-18 11:30:07.244414','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-18 11:30:07.244414',4,'PENDING',NULL,NULL),(59,'2026-06-18 11:33:12.906093',NULL,'2026-06-18 11:33:12.906093','PENDING','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-18 11:33:12.906093',4,'PENDING',NULL,NULL),(60,'2026-06-18 11:36:20.964131',NULL,'2026-06-18 11:36:20.964131','CANCELLED','150, Laluan Raia Savanna 8, Batu Gajah, Malaysia',850.00,'2026-06-18 11:36:20.964131',4,'FAILED','g98rt2kj',NULL);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `paymentId` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(10,2) DEFAULT NULL,
  `billCode` varchar(100) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `paymentStatus` varchar(50) DEFAULT NULL,
  `paymentUrl` varchar(500) DEFAULT NULL,
  `orderId` bigint NOT NULL,
  PRIMARY KEY (`paymentId`),
  KEY `FKhfe6e52jm7yfp2po54otdhmey` (`orderId`),
  CONSTRAINT `FKhfe6e52jm7yfp2po54otdhmey` FOREIGN KEY (`orderId`) REFERENCES `orders` (`orderId`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,844.90,'u8qok0jx',NULL,'2026-05-06 14:23:02.584073','PENDING','https://dev.toyyibpay.com/bill/?billCode=u8qok0jx',23),(2,844.90,'lmnhxw22',NULL,'2026-05-06 14:24:26.248311','PENDING','https://dev.toyyibpay.com/bill/?billCode=lmnhxw22',24),(3,1204.90,'6nq1r21h',NULL,'2026-05-06 14:27:55.052372','PENDING','https://dev.toyyibpay.com/bill/?billCode=6nq1r21h',25),(4,1204.90,'lpwqsdby',NULL,'2026-05-06 14:31:47.060805','PENDING','https://dev.toyyibpay.com/bill/?billCode=lpwqsdby',26),(5,1204.90,'q3lb6pfl',NULL,'2026-05-06 14:35:01.133048','PENDING','https://dev.toyyibpay.com/bill/?billCode=q3lb6pfl',27),(6,844.90,'407fk5f4','2026-05-06 15:09:58.605054','2026-05-06 15:09:24.013935','COMPLETED','https://dev.toyyibpay.com/bill/?billCode=407fk5f4',28),(7,2104.90,'wvr9kcf3','2026-06-03 01:05:25.803172','2026-06-03 01:03:40.380407','COMPLETED','https://dev.toyyibpay.com/bill/?billCode=wvr9kcf3',29),(8,824.90,'fqcr79ma','2026-06-04 01:27:09.102356','2026-06-04 01:26:15.132103','COMPLETED','https://dev.toyyibpay.com/bill/?billCode=fqcr79ma',30),(9,1274.90,'wwl9ozqr',NULL,'2026-06-04 01:42:34.664944','PENDING','https://dev.toyyibpay.com/bill/?billCode=wwl9ozqr',33),(10,1284.90,'wlmmb72a','2026-06-04 02:08:40.069275','2026-06-04 02:07:31.719832','COMPLETED','https://dev.toyyibpay.com/bill/?billCode=wlmmb72a',34),(11,854.90,'lvnw0j9j',NULL,'2026-06-10 16:37:41.466053','PENDING','https://dev.toyyibpay.com/bill/?billCode=lvnw0j9j',35),(12,854.90,'kbyq1odz',NULL,'2026-06-11 03:28:33.919266','PENDING','https://dev.toyyibpay.com/bill/?billCode=kbyq1odz',36),(13,854.90,'kcr4k5az','2026-06-11 03:30:48.993108','2026-06-11 03:30:16.371889','COMPLETED','https://dev.toyyibpay.com/bill/?billCode=kcr4k5az',37),(14,850.00,'ebok316q','2026-06-18 05:32:06.388016','2026-06-18 05:31:11.523680','COMPLETED','https://dev.toyyibpay.com/bill/?billCode=ebok316q',51),(15,148.00,'ys1qv1r8','2026-06-18 05:45:44.356636','2026-06-18 05:45:15.596670','COMPLETED','https://dev.toyyibpay.com/bill/?billCode=ys1qv1r8',52),(16,578.00,'hhxf81fy','2026-06-18 05:48:12.217408','2026-06-18 05:47:47.367319','COMPLETED','https://dev.toyyibpay.com/bill/?billCode=hhxf81fy',53),(17,279.00,'boab882y','2026-06-18 11:02:59.870478','2026-06-18 11:01:59.401556','COMPLETED','https://dev.toyyibpay.com/bill/?billCode=boab882y',54),(18,625.00,'0e1kvjox',NULL,'2026-06-18 11:04:27.514321','FAILED','https://dev.toyyibpay.com/bill/?billCode=0e1kvjox',55),(19,130.00,'se3skdzt',NULL,'2026-06-18 11:06:47.358622','PENDING','https://dev.toyyibpay.com/bill/?billCode=se3skdzt',56),(20,1200.00,'563v2t58',NULL,'2026-06-18 11:08:00.264593','FAILED','https://dev.toyyibpay.com/bill/?billCode=563v2t58',57),(21,850.00,'g98rt2kj',NULL,'2026-06-18 11:36:21.032639','FAILED','https://dev.toyyibpay.com/bill/?billCode=g98rt2kj',60);
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `productId` varchar(50) NOT NULL,
  `approvalStatus` varchar(20) DEFAULT NULL,
  `baseNotes` varchar(50) DEFAULT NULL,
  `category` varchar(100) NOT NULL,
  `createdAt` datetime(6) DEFAULT NULL,
  `description` text,
  `middleNotes` varchar(50) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `prodimage` varchar(200) NOT NULL,
  `productName` varchar(100) NOT NULL,
  `stock` int DEFAULT NULL,
  `topNotes` varchar(50) DEFAULT NULL,
  `updatedAt` datetime(6) DEFAULT NULL,
  `userId` int NOT NULL,
  `fragranceFamily` varchar(50) DEFAULT NULL,
  `genderExpression` varchar(50) DEFAULT NULL,
  `intensity` varchar(50) DEFAULT NULL,
  `longevity` varchar(50) DEFAULT NULL,
  `naturalness` varchar(50) DEFAULT NULL,
  `occasion` varchar(50) DEFAULT NULL,
  `season` varchar(50) DEFAULT NULL,
  `sillage` varchar(50) DEFAULT NULL,
  `sweetness` int DEFAULT NULL,
  PRIMARY KEY (`productId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES ('P1','approved','Amber|Musk|Leather','unisex','2026-05-06 15:49:54.480461','This fragrance reveals luminous facets of white flowers, elevated by light woods. Soft and sensual, it melts into the skin in an organic body-to-body embrace. Like the Saddle bag, the fragrance becomes an extension of the body—a trompe-l’œil leather, worn like a second skin.','Jasmine',1200,'/uploads/products/90f679be-5bd7-45e5-a361-f2e645bdb4d4.jpg','Cuir Saddle',38,'Nutmeg','2026-06-22 12:12:53.699117',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P10','approved','Musk|Sandalwood','male','2026-06-09 10:34:10.832432','It features an icy burst of citrus and aquatic notes, layered with aromatic spices, and is anchored by a warm, woody base. This fragrance is crafted for the man who embraces both freshness and depth, making it perfect for daytime wear, office settings, or warm-weather occasions. The scent exudes clean sophistication and youthful confidence.','',149,'/uploads/products/5dcfeb33-327e-499f-88cd-1b8aa7b2431d.jpg','White Ice ',20,'Bergamot|Orange|Blackcurrant','2026-06-09 10:49:20.685410',7,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P11','approved','Cedarwood|Patchouli|Incense','male','2026-06-09 10:38:13.390952','Red Leather is a bold and daring fragrance that embodies strength, confidence, and timeless allure. It opens with a striking blend of citrus, orange, and thyme, delivering a fresh yet subtly spiced introduction that ignites the senses. Red Leather is a scent for those who command attention effortlessly, embracing their power and sophistication with every step','Geranium|Rose',130,'/uploads/products/79f3871b-9a83-489d-b6ab-4ec1e5185077.jpg','Red Leather',60,'Orange','2026-06-09 10:49:24.104630',7,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P12','approved','Musk|Patchouli|Amber','unisex','2026-06-09 10:46:12.947141','Freaking Amazing is a daring and unforgettable fragrance that captures the essence of boldness, freshness, and adventure. It is for those who embrace life fearlessly, exuding confidence and a sense of adventure with every step. Perfect for those unforgettable moments, this fragrance leaves a lasting impression that will be hard to forget.','Lavender',128,'/uploads/products/096939bc-619e-4c8d-af6d-240127fcaf58.jpg','Freaking Amazing',45,'Bergamot|Orange','2026-06-09 10:49:26.693498',7,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P13','approved','Sandalwood|Cedarwood|Musk','unisex','2026-06-09 10:48:58.554095','Be Witched Aurora is a luminous and enchanting fragrance that captures the mystique of a magical twilight. It is a scent for those who embrace mystery and elegance, perfect for both day and evening wear. It leaves a mesmerizing impression, capturing the balance of light and dark in a captivating, unforgettable fragrance.','Rose',148,'/uploads/products/f6a52786-1e66-445e-a33b-a7ce35a610fe.jpg','Bewitched Aurora',120,'Lemon|Bergamot','2026-06-09 10:49:29.277526',7,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P2','approved','Vanilla|Cedarwood','unisex','2026-05-06 15:55:02.063297','Powerful and woody, the Bois Talisman silhouette is delicately lined with a trilogy of sweet vanilla notes. An olfactory composition in which the cedar note serves as a mystical shrine for an opulent vanilla note, unfurled in all its facets, this scent is to be worn like a fragrance talisman.','',1200,'/uploads/products/655f8fd7-23ab-44a6-9a90-2ee90138511d.webp','Bois Talisman',80,'','2026-06-22 12:30:01.502693',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P3','approved','Patchouli|Cedarwood','unisex','2026-05-06 15:57:24.744139','A sensation of fullness and volume is thus created, a unisex silhouette with multiple olfactory hues that come together in a mysterious chypre trail punctuated with notes of citrus, rose, violet and then oak moss.','Iris|Violet|Rose',1250,'/uploads/products/57c9448e-3785-424f-88cb-cb3f562df144.webp','Gris Dior',49,'Bergamot|Grapefruit','2026-06-22 12:32:15.229963',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P4','approved','Patchouli|Vanilla|Amber','female','2026-06-02 23:46:57.683180','The perfumer reinventes the legendary chypre fragrance of Miss Dior in a composition that blends floral and fruity accords with voluptuous notes of ambery woods. In homage to the jasmine of the original 1947 Miss Dior fragrance, a jasmine with rare facets, obtained through a special extraction process, was chosen by Francis Kurkdjian for Dior. It reveals a surprising rosy and fruity note that contrasts with the woody tones and is brightened by hints of mandarin.\r\n','Jasmine|Peony',850,'/uploads/products/279da5ab-e02b-4d6d-91f9-2c7bfb991fe4.webp','Miss Dior Parfum',49,'Bergamot','2026-06-02 23:47:44.129827',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P5','approved','Musk|Vanilla|Cedarwood','female','2026-06-03 16:43:20.048317','The pleasure of a reinvented J\'adore bouquet with a strong natural focus: Neroli from Vallauris injects its full freshness into J\'adore Parfum d\'eau, while sunny notes of jasmine sambac meld with velvety notes of Chinese magnolia. A genuine ode to the white flowers of J\'adore in a fresh and spontaneous interpretation.','Jasmine|Tuberose|Freesia',450,'/uploads/products/dccd9d35-e01a-4bb0-928f-bf91ff954178.jpg','J\'adore Parfum d\'eau',20,'Bergamot|Peach|Orange','2026-06-03 16:59:40.799788',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P6','approved','Vetiver|Patchouli|Sandalwood','male','2026-06-03 17:09:55.093627','Sauvage Elixir is an extraordinarily¹concentrated fragrance steeped in the emblematic freshness of Sauvage with an intoxicating heart of spices, a \"tailor-made\" lavender essence and a blend of rich woods forming the signature of its powerful, lavish and captivating trail.','Lavender|Geranium',850,'/uploads/products/1144d946-c8af-4637-a855-6402a9f16078.webp','Sauvage Elixir',50,'Bergamot','2026-06-22 12:41:28.249952',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P7','approved','Leather','male','2026-06-03 17:17:26.384985','Alone in the face of the majestic elements, the Fahrenheit man incarnates a thirst for the absolute. Timeless and universal, Fahrenheit is a fragrance that transcends time and trends to forge its own territory. A unique, contrasting olfactory signature with a powerful, lingering trail. The fragrance is structured around fresh notes of Sicilian Mandarin, a surprising blend of masculine Wood and Leather notes, and a unique Violet accord.','Violet',625,'/uploads/products/40ea7964-46c8-4e22-a38d-c52eb317ad5e.webp','Fahrenheit Eau de Toilette',100,'Orange','2026-06-22 12:43:14.578995',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P8','approved','Sandalwood|Tonka|Vanilla','male','2026-06-03 17:49:25.401676','Original and harmonious, Dune pour Homme takes its inspiration from breezy seaside escapes. Its composition associates fresh, woody and oceanic notes in a fragrance that is serene and in harmony with nature.','Rose',650,'/uploads/products/79d8e1e4-946a-4038-88d0-c96aa66998c9.webp','Dune Pour Homme',24,'Basil','2026-06-22 12:46:19.037658',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('P9','approved','Tonka|Vanilla|Vetiver','female','2026-06-09 10:25:30.200035','Hush Lush Whisper is a fragrance that speaks softly yet powerfully, perfect for those who desire a refined, elegant scent that is both fresh and captivating. Ideal for daily wear or special occasions, it envelops the wearer in a delicate and unforgettable aura.','Iris|Peony|Rose',189,'/uploads/products/2dd77832-3088-4317-967d-0754cc620439.jpg','Hush Lush Whisper',150,'Apple|Raspberry|Orange','2026-06-09 10:49:31.871838',7,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `promotion_voucher`
--

DROP TABLE IF EXISTS `promotion_voucher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `promotion_voucher` (
  `voucher_id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `current_usage` int DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount_type` enum('FIXED_AMOUNT','PERCENTAGE') NOT NULL,
  `discount_value` decimal(38,2) NOT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT NULL,
  `max_usage` int DEFAULT NULL,
  `min_purchase_amount` decimal(38,2) DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `supplier_id` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `voucher_code` varchar(50) NOT NULL,
  PRIMARY KEY (`voucher_id`),
  UNIQUE KEY `UKt0xt1agvqe50nutlljuxi1hnf` (`voucher_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotion_voucher`
--

LOCK TABLES `promotion_voucher` WRITE;
/*!40000 ALTER TABLE `promotion_voucher` DISABLE KEYS */;
INSERT INTO `promotion_voucher` VALUES (1,'2026-04-21 07:24:28.152021',6,'Summer discount','FIXED_AMOUNT',30.00,'2026-06-30 01:49:00.000000',_binary '',20,800.00,'2026-06-01 01:49:00.000000',3,'2026-06-04 01:42:34.637495','AAA'),(2,'2026-06-03 17:36:30.950226',0,'Happy New Year','PERCENTAGE',20.00,NULL,_binary '',5,500.00,NULL,3,'2026-06-03 17:52:14.555076','BBB'),(3,'2026-06-04 01:44:24.591859',0,'Winter Seasonal ','FIXED_AMOUNT',15.00,'2026-06-11 01:44:00.000000',_binary '',10,200.00,'2026-06-04 01:44:00.000000',3,'2026-06-04 01:44:24.591859','CCC');
/*!40000 ALTER TABLE `promotion_voucher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `reviewId` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `helpfulCount` int DEFAULT NULL,
  `rating` int DEFAULT NULL,
  `reviewStatus` varchar(50) DEFAULT NULL,
  `reviewText` varchar(1000) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `customerId` int NOT NULL,
  `productId` char(3) NOT NULL,
  PRIMARY KEY (`reviewId`),
  KEY `FKgc29k07q360imu02vxo5t6ugy` (`customerId`),
  KEY `FKtdg94lyg1t8x0khj3p9g9y546` (`productId`),
  CONSTRAINT `FKgc29k07q360imu02vxo5t6ugy` FOREIGN KEY (`customerId`) REFERENCES `customer` (`customerid`),
  CONSTRAINT `FKtdg94lyg1t8x0khj3p9g9y546` FOREIGN KEY (`productId`) REFERENCES `product` (`productId`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (1,'2026-05-03 07:11:29.549167',0,5,'APPROVED','very nice!','2026-05-03 07:11:29.549167',1,'P2'),(2,'2026-05-06 15:35:37.155009',0,2,'APPROVED','defo not worth the price','2026-05-06 15:35:37.155009',2,'P2'),(3,'2026-06-03 01:29:00.939488',0,5,'APPROVED','Lasts on me all day long! I love it!','2026-06-03 01:29:00.939488',1,'P4'),(4,'2026-06-08 09:19:16.918225',0,4,'APPROVED','love the drydown on me, very long-lasting','2026-06-08 09:19:16.918225',2,'P1'),(5,'2026-06-18 06:56:09.358160',0,4,'APPROVED','it does not last veryy long but it suits my preference','2026-06-18 06:56:09.358160',1,'P5');
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review_reply`
--

DROP TABLE IF EXISTS `review_reply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_reply` (
  `replyId` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `replyText` varchar(1000) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `reviewId` bigint NOT NULL,
  `supplierId` int NOT NULL,
  PRIMARY KEY (`replyId`),
  UNIQUE KEY `UKpm3cbtkn9pyrfy667os5l48wr` (`reviewId`),
  KEY `FKs7okpj5l72x7khlsbf0v00c9q` (`supplierId`),
  CONSTRAINT `FK3v46twuvrnybatislbmxpka34` FOREIGN KEY (`reviewId`) REFERENCES `review` (`reviewId`),
  CONSTRAINT `FKs7okpj5l72x7khlsbf0v00c9q` FOREIGN KEY (`supplierId`) REFERENCES `supplier` (`supplierid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review_reply`
--

LOCK TABLES `review_reply` WRITE;
/*!40000 ALTER TABLE `review_reply` DISABLE KEYS */;
INSERT INTO `review_reply` VALUES (1,'2026-05-03 08:02:12.962318','Thank you for reviewing! Your support means a lot','2026-05-03 08:02:12.962318',1,1);
/*!40000 ALTER TABLE `review_reply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart`
--

DROP TABLE IF EXISTS `shopping_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart` (
  `cartId` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `customerId` int NOT NULL,
  PRIMARY KEY (`cartId`),
  KEY `FK8echgp1x4kwf8y65pb2oeevgr` (`customerId`),
  CONSTRAINT `FK8echgp1x4kwf8y65pb2oeevgr` FOREIGN KEY (`customerId`) REFERENCES `customer` (`customerid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart`
--

LOCK TABLES `shopping_cart` WRITE;
/*!40000 ALTER TABLE `shopping_cart` DISABLE KEYS */;
INSERT INTO `shopping_cart` VALUES (1,'2026-04-17 15:05:49.363512','2026-06-18 05:47:25.098989',1),(2,'2026-05-02 06:57:56.751294','2026-06-18 10:40:52.363917',2);
/*!40000 ALTER TABLE `shopping_cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier`
--

DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier` (
  `supplierid` int NOT NULL AUTO_INCREMENT,
  `approvalDate` datetime(6) DEFAULT NULL,
  `approvalNotes` text,
  `approvalStatus` varchar(20) DEFAULT NULL,
  `brandName` varchar(100) NOT NULL,
  `businessRegistration` varchar(255) DEFAULT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `userid` int NOT NULL,
  PRIMARY KEY (`supplierid`),
  UNIQUE KEY `UK84ob2ukf2r0sm23cyvem8ahr3` (`userid`),
  CONSTRAINT `FKtjudach3ttfflrrq39v3qmb6j` FOREIGN KEY (`userid`) REFERENCES `users` (`userid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (1,'2026-04-15 15:10:38.548929',NULL,'approved','Dior','Dior_MY_f1de03f1-42a6-42fb-8ff3-fac84415dec8.pdf','2026-04-15 15:09:58.461164','2026-04-15 15:10:38.566713',3),(2,'2026-06-09 10:22:04.275467',NULL,'approved','SugarBomb','SugarBomb_MY_94f77ea5-d8dd-413c-97fa-c70c12e1d381.pdf','2026-06-09 10:21:00.064984','2026-06-09 10:22:04.280222',7);
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `userid` int NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  PRIMARY KEY (`userid`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'2026-04-15 15:00:26.751744','ziqah@gmail.com','Haziqah123','customer','2026-04-15 15:00:26.751770','ziqah'),(2,'2026-04-15 15:02:44.000000','manager@scentify.com','Manager123','system_manager','2026-04-15 15:02:44.000000','manager'),(3,'2026-04-15 15:09:58.321564','dior@gmail.com','Dior123','supplier','2026-04-15 15:09:58.321611','Dior_MY'),(4,'2026-04-21 07:27:02.000000','member@test.com','password123','customer','2026-04-21 07:27:02.000000','member_customer'),(5,'2026-04-21 07:27:02.000000','nonmember@test.com','password123','customer','2026-04-21 07:27:02.000000','non_member_customer'),(6,'2026-06-03 02:46:00.148299','safiya@gmail.com','Sasafiya2024','customer','2026-06-03 02:46:00.148299','safiya'),(7,'2026-06-09 10:20:59.974951','sugarbomb@gmail.com','Sugarbomb123','supplier','2026-06-09 10:20:59.974951','SugarBomb_MY'),(8,'2026-06-10 16:45:33.591260','aqila@gmail.com','Aqilah123','customer','2026-06-10 16:45:33.591260','qila');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucher_product`
--

DROP TABLE IF EXISTS `voucher_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher_product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `product_id` varchar(255) NOT NULL,
  `voucher_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1dc5pf6viip53x2j6cr1jhjiq` (`voucher_id`),
  CONSTRAINT `FK1dc5pf6viip53x2j6cr1jhjiq` FOREIGN KEY (`voucher_id`) REFERENCES `promotion_voucher` (`voucher_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucher_product`
--

LOCK TABLES `voucher_product` WRITE;
/*!40000 ALTER TABLE `voucher_product` DISABLE KEYS */;
INSERT INTO `voucher_product` VALUES (8,'2026-06-03 01:50:28.028814','P1',1),(9,'2026-06-03 01:50:28.044739','P2',1),(10,'2026-06-03 17:36:40.892200','P6',2),(11,'2026-06-03 17:36:40.908491','P7',2),(12,'2026-06-04 01:44:37.800354','P7',3);
/*!40000 ALTER TABLE `voucher_product` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-22 18:27:41
