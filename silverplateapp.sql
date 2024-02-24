-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 11, 2023 at 01:00 PM
-- Server version: 10.4.28-MariaDB
-- PHP Version: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `silverplateapp`
--

-- --------------------------------------------------------

--
-- Table structure for table `inventory`
--

CREATE TABLE `inventory` (
  `inventory_id` int(11) NOT NULL,
  `ingredient_name` varchar(50) NOT NULL,
  `inventory_quantity` int(11) NOT NULL,
  `inventory_category` varchar(30) NOT NULL,
  `date_received` date NOT NULL,
  `supplier_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `inventory`
--

INSERT INTO `inventory` (`inventory_id`, `ingredient_name`, `inventory_quantity`, `inventory_category`, `date_received`, `supplier_id`) VALUES
(1, 'Chicken Pieces (Small)', 30, 'Poultry', '2023-05-12', 2),
(2, 'Chicken Pieces (Large)', 23, 'Poultry', '2023-05-12', 2),
(3, 'Burger Bun', 60, 'Dry Good', '2023-05-04', 3),
(4, 'Coke 250 mL', 100, 'Beverages', '2023-05-01', 1),
(6, 'Sprite 250mL', 500, 'Beverages', '2023-06-09', 1),
(7, 'Toast', 20, 'Dry Good', '2023-06-10', 3),
(8, 'Large Eggs', 30, 'Poultry', '2023-06-10', 4),
(9, 'Baked Beans (100g)', 25, 'Vegetables', '2023-06-10', 4),
(10, 'Sausage', 15, 'Meat', '2023-06-10', 2),
(11, 'Coffee (1 Shot)', 100, 'Beverages', '2023-06-10', 1),
(12, 'Burger Patty', 10, 'Meat', '2023-06-10', 7),
(13, 'Shredded Iceberg Lettuce (50g)', 50, 'Vegetables', '2023-06-10', 7),
(14, 'Cheese Slices', 50, 'Dairy', '2023-06-10', 7),
(15, 'Tomato Slices', 50, 'Fruits', '2023-06-10', 8),
(16, 'Ice Cream Scoop (Vanilla)', 45, 'Dairy', '2023-06-07', 4),
(17, 'Milk (1 Cup)', 30, 'Dairy', '2023-06-07', 7),
(18, 'Strawberry Flavouring (1/5 Cup)', 300, 'Dairy', '2023-06-07', 4),
(19, 'Chips (250g)', 100, 'Dry Good', '2023-06-08', 4);

-- --------------------------------------------------------

--
-- Table structure for table `product`
--

CREATE TABLE `product` (
  `product_id` int(11) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `product_price` decimal(10,2) NOT NULL,
  `product_category` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `product`
--

INSERT INTO `product` (`product_id`, `product_name`, `product_price`, `product_category`) VALUES
(1, 'Chicken Meal Deal', 13.50, 'Meal Deal'),
(3, 'English Breakfast', 25.00, 'Meal'),
(5, 'Coca-Cola 250mL', 2.50, 'Drink'),
(6, 'Strawberry Milkshake', 8.00, 'Drink'),
(7, 'Burger', 10.00, 'Meal'),
(8, 'Burger Meal', 15.00, 'Meal Deal');

-- --------------------------------------------------------

--
-- Table structure for table `product_inventory`
--

CREATE TABLE `product_inventory` (
  `prodInv_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `inventory_id` int(11) NOT NULL,
  `amount_used` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `product_inventory`
--

INSERT INTO `product_inventory` (`prodInv_id`, `product_id`, `inventory_id`, `amount_used`) VALUES
(17, 5, 4, 1),
(18, 3, 7, 2),
(19, 3, 8, 2),
(20, 3, 9, 2),
(21, 3, 10, 2),
(22, 1, 2, 5),
(23, 1, 1, 5),
(24, 6, 17, 2),
(25, 6, 16, 1),
(26, 6, 18, 1),
(27, 7, 3, 2),
(28, 7, 12, 1),
(29, 7, 13, 1),
(30, 7, 14, 1),
(31, 7, 15, 2),
(32, 8, 12, 2),
(33, 8, 14, 1),
(34, 8, 12, 1),
(35, 8, 13, 1),
(36, 8, 19, 1),
(37, 8, 4, 1);

-- --------------------------------------------------------

--
-- Table structure for table `sales`
--

CREATE TABLE `sales` (
  `sale_id` int(11) NOT NULL,
  `sale_date` date NOT NULL,
  `sale_price` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sales`
--

INSERT INTO `sales` (`sale_id`, `sale_date`, `sale_price`) VALUES
(1, '2023-06-10', 13.50),
(2, '2023-06-09', 25.00),
(3, '2023-06-11', 25.00),
(5, '2023-06-09', 56.00),
(6, '2023-06-11', 54.50),
(7, '2023-06-10', 54.00);

-- --------------------------------------------------------

--
-- Table structure for table `sales_product`
--

CREATE TABLE `sales_product` (
  `salesProd_id` int(11) NOT NULL,
  `sales_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `sale_quantity` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sales_product`
--

INSERT INTO `sales_product` (`salesProd_id`, `sales_id`, `product_id`, `sale_quantity`) VALUES
(2, 2, 3, 1),
(3, 2, 5, 2),
(4, 3, 7, 2),
(5, 3, 5, 2),
(8, 5, 5, 3),
(9, 5, 3, 1),
(10, 5, 1, 1),
(11, 5, 7, 1),
(16, 6, 5, 2),
(17, 6, 6, 2),
(18, 6, 7, 2),
(19, 6, 1, 1),
(20, 6, 3, 1),
(21, 1, 1, 1),
(22, 1, 5, 3),
(23, 1, 7, 2),
(24, 7, 1, 4);

-- --------------------------------------------------------

--
-- Table structure for table `staff`
--

CREATE TABLE `staff` (
  `staff_id` int(11) NOT NULL,
  `staff_name` varchar(50) NOT NULL,
  `staff_address` varchar(100) NOT NULL,
  `staff_email` varchar(100) NOT NULL,
  `staff_phone` varchar(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `staff`
--

INSERT INTO `staff` (`staff_id`, `staff_name`, `staff_address`, `staff_email`, `staff_phone`) VALUES
(1, 'Huy Ngo', '6 Santolina Place', 'hngo24@gmail.com', '0432765930'),
(2, 'Linda Pham', '6 Santolina Place', 'lindapham2@hotmail.com', '0425335280'),
(3, 'Huynh Thi Pham', '189 Arrabri Avenue', 'dosja8988@gmail.com', '0421248593'),
(4, 'Kal Machinsky', '103 Pateena Street', 'moola15@outlook.com', '0463752365');

-- --------------------------------------------------------

--
-- Table structure for table `staffshift`
--

CREATE TABLE `staffshift` (
  `staff_id` int(11) NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `shift_date` date NOT NULL,
  `shift_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `staffshift`
--

INSERT INTO `staffshift` (`staff_id`, `start_time`, `end_time`, `shift_date`, `shift_id`) VALUES
(1, '08:00:00', '16:00:00', '2023-06-10', 1),
(4, '08:00:00', '14:15:00', '2023-06-10', 20),
(1, '07:45:00', '13:00:00', '2023-06-11', 24),
(2, '07:15:00', '13:45:00', '2023-06-12', 27);

-- --------------------------------------------------------

--
-- Table structure for table `supplier`
--

CREATE TABLE `supplier` (
  `supplier_id` int(11) NOT NULL,
  `supplier_name` varchar(50) NOT NULL,
  `supplier_email` varchar(100) NOT NULL,
  `supplier_phone` varchar(10) NOT NULL,
  `supplier_category` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `supplier`
--

INSERT INTO `supplier` (`supplier_id`, `supplier_name`, `supplier_email`, `supplier_phone`, `supplier_category`) VALUES
(1, 'Coca-Cola Amatil', 'Consumer_information@ccamatil.com', '1800025123', 'Beverages'),
(2, 'Inghams Lyton', 'olytton@inghams.com.au', '0730972032', 'Poultry'),
(3, 'Flour Shop Bakery', 'sales@fsb.com.au', '0415724528', 'Dry Good'),
(4, 'Costco Wholesale', 'costcosales@costco.support', '0492039127', 'Vegetables'),
(7, 'Coles Richlands', 'colesrichlands@coles.support', '0420941029', 'Beverages'),
(8, 'Woolworths Group', 'woolworths@woolworths.enterprisesupport', '1800048240', 'Dairy');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `inventory`
--
ALTER TABLE `inventory`
  ADD PRIMARY KEY (`inventory_id`),
  ADD KEY `supplier_id` (`supplier_id`);

--
-- Indexes for table `product`
--
ALTER TABLE `product`
  ADD PRIMARY KEY (`product_id`);

--
-- Indexes for table `product_inventory`
--
ALTER TABLE `product_inventory`
  ADD PRIMARY KEY (`prodInv_id`),
  ADD KEY `product_id` (`product_id`),
  ADD KEY `inventory_id` (`inventory_id`);

--
-- Indexes for table `sales`
--
ALTER TABLE `sales`
  ADD PRIMARY KEY (`sale_id`);

--
-- Indexes for table `sales_product`
--
ALTER TABLE `sales_product`
  ADD PRIMARY KEY (`salesProd_id`),
  ADD KEY `product_id` (`product_id`),
  ADD KEY `sales_id` (`sales_id`);

--
-- Indexes for table `staff`
--
ALTER TABLE `staff`
  ADD PRIMARY KEY (`staff_id`);

--
-- Indexes for table `staffshift`
--
ALTER TABLE `staffshift`
  ADD PRIMARY KEY (`shift_id`),
  ADD UNIQUE KEY `staff_shift_unique` (`staff_id`,`start_time`,`shift_date`),
  ADD KEY `staff_id` (`staff_id`);

--
-- Indexes for table `supplier`
--
ALTER TABLE `supplier`
  ADD PRIMARY KEY (`supplier_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `inventory`
--
ALTER TABLE `inventory`
  MODIFY `inventory_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `product`
--
ALTER TABLE `product`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `product_inventory`
--
ALTER TABLE `product_inventory`
  MODIFY `prodInv_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=38;

--
-- AUTO_INCREMENT for table `sales`
--
ALTER TABLE `sales`
  MODIFY `sale_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `sales_product`
--
ALTER TABLE `sales_product`
  MODIFY `salesProd_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT for table `staff`
--
ALTER TABLE `staff`
  MODIFY `staff_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `staffshift`
--
ALTER TABLE `staffshift`
  MODIFY `shift_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=47;

--
-- AUTO_INCREMENT for table `supplier`
--
ALTER TABLE `supplier`
  MODIFY `supplier_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `inventory`
--
ALTER TABLE `inventory`
  ADD CONSTRAINT `inventory_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`supplier_id`);

--
-- Constraints for table `product_inventory`
--
ALTER TABLE `product_inventory`
  ADD CONSTRAINT `product_inventory_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `product_inventory_ibfk_2` FOREIGN KEY (`inventory_id`) REFERENCES `inventory` (`inventory_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `sales_product`
--
ALTER TABLE `sales_product`
  ADD CONSTRAINT `sales_product_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`),
  ADD CONSTRAINT `sales_product_ibfk_2` FOREIGN KEY (`sales_id`) REFERENCES `sales` (`sale_id`);

--
-- Constraints for table `staffshift`
--
ALTER TABLE `staffshift`
  ADD CONSTRAINT `staffshift_ibfk_1` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
