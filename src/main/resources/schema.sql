CREATE TABLE IF NOT EXISTS `orders` (
   `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
   `customer_id` BIGINT NOT NULL,
   `asset_name` VARCHAR(255) NOT NULL,
    `order_side` ENUM('BUY', 'SELL') NOT NULL,
    `size` DECIMAL(20, 8) NOT NULL,
    `price` DECIMAL(20, 8) NOT NULL,
    `status` ENUM('PENDING', 'MATCHED', 'CANCELLED') NOT NULL,
    `create_date` DATETIME NOT NULL,
    CONSTRAINT unique_customer_order UNIQUE (customer_id, create_date)
    );

CREATE TABLE IF NOT EXISTS `asset` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `customer_id` BIGINT NOT NULL,
    `asset_name` VARCHAR(255) NOT NULL,
    `size` DECIMAL(20, 8) NOT NULL,
    `usable_size` DECIMAL(20, 8) NOT NULL,
    CONSTRAINT unique_customer_asset UNIQUE (customer_id, asset_name)
    );

CREATE TABLE IF NOT EXISTS `customer` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(255) NOT NULL
);

INSERT INTO `customer` (`username`, `password`, `role`) SELECT 'admin','$2a$10$/xik00KrhYg6dppvAW2uUenhyG.vSlRicilxfmgbCzFwnAfsUaDf2', 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM customer where username = 'admin'); --admin123
INSERT INTO `customer` (`username`, `password`, `role`) SELECT 'customer1','$2a$10$m3/k4XhBjYmq1eEIEL0DBOn6jbAHVpKi.u1j1HOWrM5CgvzHP43TK', 'USER' WHERE NOT EXISTS (SELECT 1 FROM customer where username = 'customer1'); --password123
INSERT INTO `customer` (`username`, `password`, `role`) SELECT 'customer2','$2a$10$KlxoGTqzNJDJNfOC8c2DV.m3St/.ORrRaOaiuKfUMAYrD5Iu/9Pui', 'USER' WHERE NOT EXISTS (SELECT 1 FROM customer where username = 'customer2'); --password234
INSERT INTO `customer` (`username`, `password`, `role`) SELECT 'customer3','$2a$10$11n/Hg1OXkRGHYowJogWLOjL.P8IiV.n09j5Fqe1eHRwJmuLGoFN6', 'USER' WHERE NOT EXISTS (SELECT 1 FROM customer where username = 'customer3'); --password345
INSERT INTO `customer` (`username`, `password`, `role`) SELECT 'customer4','$2a$10$jG97fNRg.Dkzm8h6MIOkquDJgCvOYYZ2BlTlgjel11i9aIiorEsUe', 'USER' WHERE NOT EXISTS (SELECT 1 FROM customer where username = 'customer4'); --password456

INSERT INTO `asset` (`customer_id`, `asset_name`, `size`, `usable_size`) SELECT 2,'TRY', 10000.00, 10000.00 WHERE NOT EXISTS (SELECT 1 FROM asset where customer_id = 2 and asset_name = 'TRY'); --define default TRY size
INSERT INTO `asset` (`customer_id`, `asset_name`, `size`, `usable_size`) SELECT 3,'TRY', 10000.00, 10000.00 WHERE NOT EXISTS (SELECT 1 FROM asset where customer_id = 3 and asset_name = 'TRY'); --define default TRY size
INSERT INTO `asset` (`customer_id`, `asset_name`, `size`, `usable_size`) SELECT 4,'TRY', 10000.00, 10000.00 WHERE NOT EXISTS (SELECT 1 FROM asset where customer_id = 4 and asset_name = 'TRY'); --define default TRY size
INSERT INTO `asset` (`customer_id`, `asset_name`, `size`, `usable_size`) SELECT 5,'TRY', 10000.00, 10000.00 WHERE NOT EXISTS (SELECT 1 FROM asset where customer_id = 5 and asset_name = 'TRY'); --define default TRY size