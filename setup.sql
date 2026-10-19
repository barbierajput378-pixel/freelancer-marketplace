-- ============================================================
-- Freelancer Marketplace — full setup script for Aiven MySQL
-- Run this once against your new Aiven MySQL service.
-- Includes: schema, sample data, procedures, functions, triggers
-- (copied from Assignment 7, unchanged — real MySQL = no rewrite needed)
-- ============================================================

CREATE DATABASE IF NOT EXISTS FreelancerMarketplace;
USE FreelancerMarketplace;

-- ---------- TABLES ----------
CREATE TABLE Clients (
    client_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    company_name VARCHAR(100) NOT NULL
);

CREATE TABLE Freelancers (
    freelancer_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    avg_rating DECIMAL(2,1) CHECK (avg_rating BETWEEN 0 AND 5)
);

CREATE TABLE Jobs (
    job_id INT PRIMARY KEY,
    client_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    budget_max DECIMAL(10,2) CHECK (budget_max > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'Open',
    CONSTRAINT chk_jobs_status CHECK (status IN ('Open', 'Awarded', 'Completed', 'Cancelled')),
    FOREIGN KEY (client_id) REFERENCES Clients(client_id)
);

CREATE TABLE Offers (
    offer_id INT PRIMARY KEY,
    job_id INT NOT NULL,
    freelancer_id INT NOT NULL,
    offer_amount DECIMAL(10,2) CHECK (offer_amount > 0),
    proposed_duration INT CHECK (proposed_duration > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    CONSTRAINT chk_offers_status CHECK (status IN ('Pending', 'Selected', 'Rejected')),
    FOREIGN KEY (job_id) REFERENCES Jobs(job_id),
    FOREIGN KEY (freelancer_id) REFERENCES Freelancers(freelancer_id)
);

CREATE TABLE Contracts (
    contract_id INT PRIMARY KEY,
    job_id INT UNIQUE NOT NULL,
    freelancer_id INT NOT NULL,
    final_amount DECIMAL(10,2) CHECK (final_amount > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'In Progress',
    CONSTRAINT chk_contracts_status CHECK (status IN ('In Progress', 'Completed', 'Cancelled')),
    FOREIGN KEY (job_id) REFERENCES Jobs(job_id),
    FOREIGN KEY (freelancer_id) REFERENCES Freelancers(freelancer_id)
);

-- ---------- SAMPLE DATA ----------
INSERT INTO Clients (client_id, name, email, company_name) VALUES
(101, 'Rahul Sharma', 'rahul@techsoft.com', 'TechSoft Pvt Ltd'),
(102, 'Priya Mehta', 'priya@innovate.com', 'Innovate Solutions'),
(103, 'Amit Verma', 'amit@digitalhub.com', 'Digital Hub'),
(104, 'Neha Joshi', 'neha@webworks.com', 'WebWorks'),
(105, 'Rohan Kulkarni', 'rohan@nextgen.com', 'NextGen Tech'),
(106, 'Snehal Patil', 'snehal@cloudx.com', 'CloudX'),
(107, 'Ananya Desai', 'ananya@codecraft.com', 'CodeCraft'),
(108, 'Karan Gupta', 'karan@futureit.com', 'Future IT'),
(109, 'Meera Singh', 'meera@smartapps.com', 'SmartApps'),
(110, 'Vikram Nair', 'vikram@byteworld.com', 'ByteWorld'),
(111, 'Sakshi Pawar', 'sakshi@alphasoft.com', 'AlphaSoft'),
(112, 'Arjun Patil', 'arjun@codenest.com', 'CodeNest'),
(113, 'Nisha Reddy', 'nisha@visiontech.com', 'Vision Tech'),
(114, 'Yash Shah', 'yash@pixelworks.com', 'Pixel Works'),
(115, 'Pooja Kulkarni', 'pooja@webnova.com', 'WebNova'),
(116, 'Harsh Gupta', 'harsh@dataspark.com', 'DataSpark'),
(117, 'Simran Kaur', 'simran@cybernet.com', 'CyberNet'),
(118, 'Omkar Jadhav', 'omkar@infologic.com', 'InfoLogic'),
(119, 'Ritika Sinha', 'ritika@softedge.com', 'SoftEdge'),
(120, 'Manish Chavan', 'manish@quantix.com', 'Quantix Solutions'),
(121, 'Kavya Iyer', 'kavya@nexora.com', 'Nexora Technologies');

INSERT INTO Freelancers (freelancer_id, name, email, avg_rating) VALUES
(201, 'Aarav Shah', 'aarav@gmail.com', 4.8),
(202, 'Diya Kapoor', 'diya@gmail.com', 4.6),
(203, 'Rishi Patel', 'rishi@gmail.com', 4.2),
(204, 'Isha Gupta', 'isha@gmail.com', 4.9),
(205, 'Kabir Jain', 'kabir@gmail.com', 4.4),
(206, 'Sanya Roy', 'sanya@gmail.com', 4.7),
(207, 'Aditya Rao', 'aditya@gmail.com', 4.1),
(208, 'Pooja Kulkarni', 'pooja@gmail.com', 4.5),
(209, 'Nikhil Joshi', 'nikhil@gmail.com', 4.3),
(210, 'Tanvi Deshmukh', 'tanvi@gmail.com', 5.0),
(211, 'Rohan Patil', 'rohan@gmail.com', 4.0),
(212, 'Sneha More', 'snehamore@gmail.com', 4.6),
(213, 'Kunal Singh', 'kunal@gmail.com', 4.2),
(214, 'Anjali Verma', 'anjali@gmail.com', 4.8),
(215, 'Vivek Sharma', 'vivek@gmail.com', 4.1),
(216, 'Aditi Joshi', 'aditi@gmail.com', 4.7),
(217, 'Sahil Gupta', 'sahil@gmail.com', 4.3),
(218, 'Rutuja Patil', 'rutuja@gmail.com', 4.9),
(219, 'Akash Mehta', 'akash@gmail.com', 4.5),
(220, 'Mansi Kulkarni', 'mansi@gmail.com', 4.4),
(221, 'Dev Malhotra', 'dev@gmail.com', 4.8);

INSERT INTO Jobs (job_id, client_id, title, budget_max, status) VALUES
(301, 101, 'Website Development', 50000, 'Open'),
(302, 102, 'Android Application', 80000, 'Awarded'),
(303, 103, 'Logo Design', 10000, 'Open'),
(304, 104, 'Database Design', 30000, 'Awarded'),
(305, 105, 'SEO Optimization', 20000, 'Open'),
(306, 106, 'AI Chatbot Development', 120000, 'Open'),
(307, 107, 'Cloud Migration', 90000, 'Awarded'),
(308, 108, 'UI/UX Design', 25000, 'Open'),
(309, 109, 'Python Automation', 40000, 'Awarded'),
(310, 110, 'E-Commerce Website', 100000, 'Open'),
(311, 111, 'CRM System', 70000, 'Open'),
(312, 112, 'Inventory Management System', 65000, 'Awarded'),
(313, 113, 'Portfolio Website', 15000, 'Open'),
(314, 114, 'Social Media Marketing', 18000, 'Open'),
(315, 115, 'Data Analytics Dashboard', 85000, 'Awarded'),
(316, 116, 'Cyber Security Audit', 95000, 'Open'),
(317, 117, 'Machine Learning Model', 150000, 'Awarded'),
(318, 118, 'Content Writing', 12000, 'Open'),
(319, 119, 'Mobile Game Development', 130000, 'Open'),
(320, 120, 'ERP Software', 200000, 'Awarded'),
(321, 121, 'Video Editing Project', 22000, 'Open');

INSERT INTO Offers (offer_id, job_id, freelancer_id, offer_amount, proposed_duration, status) VALUES
(401, 301, 201, 45000, 20, 'Pending'),
(402, 302, 202, 76000, 30, 'Selected'),
(403, 303, 203, 9000, 10, 'Pending'),
(404, 304, 204, 28000, 15, 'Selected'),
(405, 305, 205, 18000, 12, 'Rejected'),
(406, 306, 206, 115000, 35, 'Pending'),
(407, 307, 207, 85000, 25, 'Selected'),
(408, 308, 208, 22000, 14, 'Pending'),
(409, 309, 209, 37000, 20, 'Selected'),
(410, 310, 210, 95000, 40, 'Pending'),
(411, 311, 211, 65000, 28, 'Selected'),
(412, 312, 212, 60000, 22, 'Selected'),
(413, 313, 213, 14000, 7, 'Pending'),
(414, 314, 214, 17000, 15, 'Rejected'),
(415, 315, 215, 80000, 30, 'Selected'),
(416, 316, 216, 90000, 18, 'Pending'),
(417, 317, 217, 145000, 45, 'Selected'),
(418, 318, 218, 10000, 6, 'Pending'),
(419, 319, 219, 125000, 50, 'Pending'),
(420, 320, 220, 190000, 60, 'Selected'),
(421, 321, 221, 20000, 8, 'Pending');

INSERT INTO Contracts (contract_id, job_id, freelancer_id, final_amount, status) VALUES
(501, 301, 201, 45000, 'In Progress'),
(502, 302, 202, 76000, 'Completed'),
(503, 303, 203, 9000, 'In Progress'),
(504, 304, 204, 28000, 'Completed'),
(505, 305, 205, 18000, 'Cancelled'),
(506, 306, 206, 115000, 'In Progress'),
(507, 307, 207, 85000, 'Completed'),
(508, 308, 208, 22000, 'In Progress'),
(509, 309, 209, 37000, 'Completed'),
(510, 310, 210, 95000, 'In Progress'),
(511, 311, 211, 65000, 'Completed'),
(512, 312, 212, 60000, 'Completed'),
(513, 313, 213, 14000, 'In Progress'),
(514, 314, 214, 17000, 'Cancelled'),
(515, 315, 215, 80000, 'Completed'),
(516, 316, 216, 90000, 'In Progress'),
(517, 317, 217, 145000, 'Completed'),
(518, 318, 218, 10000, 'Completed'),
(519, 319, 219, 125000, 'In Progress'),
(520, 320, 220, 190000, 'Completed'),
(521, 321, 221, 20000, 'In Progress');

-- ---------- PROCEDURES & FUNCTIONS ----------
DELIMITER $$

CREATE PROCEDURE GetJobsByStatus(IN p_status VARCHAR(20))
BEGIN
    SELECT * FROM Jobs WHERE status = p_status;
END $$

CREATE PROCEDURE AddClient(
    IN p_client_id INT, IN p_name VARCHAR(100),
    IN p_email VARCHAR(100), IN p_company_name VARCHAR(100)
)
BEGIN
    INSERT INTO Clients(client_id, name, email, company_name)
    VALUES(p_client_id, p_name, p_email, p_company_name);
END $$

CREATE FUNCTION CountOffers(p_job_id INT)
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE total_offers INT;
    SELECT COUNT(*) INTO total_offers FROM Offers WHERE job_id = p_job_id;
    RETURN total_offers;
END $$

CREATE FUNCTION AverageContractAmount()
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE avg_amount DECIMAL(10,2);
    SELECT AVG(final_amount) INTO avg_amount FROM Contracts;
    RETURN avg_amount;
END $$

-- AwardOffer — "select an offer" workflow in one transaction.
-- (Must be a procedure, not a trigger: it updates the same Offers table that
--  would fire the trigger, which MySQL forbids.)
CREATE PROCEDURE AwardOffer(IN p_offer_id INT)
BEGIN
    DECLARE v_job_id          INT;
    DECLARE v_freelancer_id   INT;
    DECLARE v_offer_amount    DECIMAL(10,2);
    DECLARE v_offer_status    VARCHAR(20);
    DECLARE v_job_status      VARCHAR(20);
    DECLARE v_new_contract_id INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT job_id, freelancer_id, offer_amount, status
      INTO v_job_id, v_freelancer_id, v_offer_amount, v_offer_status
      FROM Offers WHERE offer_id = p_offer_id FOR UPDATE;

    IF v_job_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot award: offer not found.';
    END IF;
    IF v_offer_status <> 'Pending' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot award: this offer is not Pending.';
    END IF;

    SELECT status INTO v_job_status FROM Jobs WHERE job_id = v_job_id FOR UPDATE;
    IF v_job_status <> 'Open' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot award: the job is not Open.';
    END IF;

    UPDATE Offers SET status = 'Selected' WHERE offer_id = p_offer_id;
    UPDATE Offers SET status = 'Rejected'
      WHERE job_id = v_job_id AND offer_id <> p_offer_id AND status = 'Pending';

    SELECT COALESCE(MAX(contract_id), 0) + 1 INTO v_new_contract_id FROM Contracts;
    INSERT INTO Contracts (contract_id, job_id, freelancer_id, final_amount, status)
    VALUES (v_new_contract_id, v_job_id, v_freelancer_id, v_offer_amount, 'In Progress');

    UPDATE Jobs SET status = 'Awarded' WHERE job_id = v_job_id;

    COMMIT;
END $$

-- ---------- TRIGGERS ----------
CREATE TRIGGER before_insert_jobs
BEFORE INSERT ON Jobs
FOR EACH ROW
BEGIN
    SET NEW.title = UPPER(NEW.title);
END $$

CREATE TRIGGER after_insert_contracts
AFTER INSERT ON Contracts
FOR EACH ROW
BEGIN
    IF NEW.status = 'In Progress' OR NEW.status = 'Completed' THEN
        UPDATE Jobs SET status = 'Awarded' WHERE job_id = NEW.job_id;
    END IF;
END $$

CREATE TRIGGER before_update_freelancers
BEFORE UPDATE ON Freelancers
FOR EACH ROW
BEGIN
    IF NEW.avg_rating > 5 THEN
        SET NEW.avg_rating = 5.0;
    END IF;
END $$

CREATE TRIGGER after_update_offers
AFTER UPDATE ON Offers
FOR EACH ROW
BEGIN
    IF NEW.status = 'Selected' AND OLD.status <> 'Selected' THEN
        UPDATE Jobs SET status = 'Awarded' WHERE job_id = NEW.job_id;
    END IF;
END $$

CREATE TRIGGER before_delete_contracts
BEFORE DELETE ON Contracts
FOR EACH ROW
BEGIN
    UPDATE Jobs SET status = 'Open' WHERE job_id = OLD.job_id;
END $$

CREATE TRIGGER after_delete_offers
AFTER DELETE ON Offers
FOR EACH ROW
BEGIN
    IF OLD.status = 'Selected' THEN
        UPDATE Jobs SET status = 'Open' WHERE job_id = OLD.job_id;
    END IF;
END $$

-- Reject an offer above the job budget or on a job that is not Open.
CREATE TRIGGER before_insert_offers
BEFORE INSERT ON Offers
FOR EACH ROW
BEGIN
    DECLARE v_budget DECIMAL(10,2);
    DECLARE v_status VARCHAR(20);

    SELECT budget_max, status INTO v_budget, v_status
      FROM Jobs WHERE job_id = NEW.job_id;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot add offer: that job does not exist.';
    END IF;
    IF v_status <> 'Open' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot add offer: the job is not Open.';
    END IF;
    IF NEW.offer_amount > v_budget THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot add offer: offer amount exceeds the job budget.';
    END IF;
END $$

-- Completing/cancelling a contract flows back to the job.
CREATE TRIGGER after_update_contracts
AFTER UPDATE ON Contracts
FOR EACH ROW
BEGIN
    IF NEW.status = 'Completed' AND OLD.status <> 'Completed' THEN
        UPDATE Jobs SET status = 'Completed' WHERE job_id = NEW.job_id;
    ELSEIF NEW.status = 'Cancelled' AND OLD.status <> 'Cancelled' THEN
        UPDATE Jobs SET status = 'Open' WHERE job_id = NEW.job_id;
    END IF;
END $$

DELIMITER ;
