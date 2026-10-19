-- ============================================================
-- Freelancer Marketplace — workflow + status migration
-- ------------------------------------------------------------
-- Adds:
--   1. Allowed status values (DEFAULT + CHECK) on Jobs/Offers/Contracts,
--      after first repairing blank/NULL status data.
--   2. Stored procedure AwardOffer(offer_id) — the "select an offer" workflow.
--   3. Trigger before_insert_offers  — budget / job-open guard.
--      Trigger after_update_contracts — completing/cancelling updates the job.
--
-- Safe to run more than once: every object is dropped (IF EXISTS) before it
-- is recreated, and the CHECK constraints are dropped-if-present before being
-- re-added (MySQL has no "DROP CONSTRAINT IF EXISTS", so a tiny helper
-- procedure does the conditional drop).
--
-- Existing objects from Assignment 7 are left untouched.
-- ============================================================

USE FreelancerMarketplace;

-- ------------------------------------------------------------
-- 1a. Repair existing bad data so the CHECK constraints can be added.
--     (e.g. job 322 had a blank status.)
-- ------------------------------------------------------------
UPDATE Jobs      SET status = 'Open'        WHERE status IS NULL OR TRIM(status) = '';
UPDATE Offers    SET status = 'Pending'     WHERE status IS NULL OR TRIM(status) = '';
UPDATE Contracts SET status = 'In Progress' WHERE status IS NULL OR TRIM(status) = '';

-- ------------------------------------------------------------
-- 1b. Column defaults.
-- ------------------------------------------------------------
ALTER TABLE Jobs      ALTER COLUMN status SET DEFAULT 'Open';
ALTER TABLE Offers    ALTER COLUMN status SET DEFAULT 'Pending';
ALTER TABLE Contracts ALTER COLUMN status SET DEFAULT 'In Progress';

-- ------------------------------------------------------------
-- 1c. CHECK constraints (drop-if-present, then add) via a throwaway helper.
-- ------------------------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS _apply_status_constraints $$
CREATE PROCEDURE _apply_status_constraints()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
               WHERE CONSTRAINT_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'Jobs' AND CONSTRAINT_NAME = 'chk_jobs_status') THEN
        ALTER TABLE Jobs DROP CHECK chk_jobs_status;
    END IF;
    ALTER TABLE Jobs ADD CONSTRAINT chk_jobs_status
        CHECK (status IN ('Open', 'Awarded', 'Completed', 'Cancelled'));

    IF EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
               WHERE CONSTRAINT_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'Offers' AND CONSTRAINT_NAME = 'chk_offers_status') THEN
        ALTER TABLE Offers DROP CHECK chk_offers_status;
    END IF;
    ALTER TABLE Offers ADD CONSTRAINT chk_offers_status
        CHECK (status IN ('Pending', 'Selected', 'Rejected'));

    IF EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
               WHERE CONSTRAINT_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'Contracts' AND CONSTRAINT_NAME = 'chk_contracts_status') THEN
        ALTER TABLE Contracts DROP CHECK chk_contracts_status;
    END IF;
    ALTER TABLE Contracts ADD CONSTRAINT chk_contracts_status
        CHECK (status IN ('In Progress', 'Completed', 'Cancelled'));
END $$

CALL _apply_status_constraints() $$
DROP PROCEDURE IF EXISTS _apply_status_constraints $$

-- ------------------------------------------------------------
-- 2. AwardOffer — the "select an offer" workflow, in one transaction.
--    (A trigger cannot do this because it would have to UPDATE the same
--     Offers table it fires on, which MySQL forbids — hence a procedure.)
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS AwardOffer $$
CREATE PROCEDURE AwardOffer(IN p_offer_id INT)
BEGIN
    DECLARE v_job_id         INT;
    DECLARE v_freelancer_id  INT;
    DECLARE v_offer_amount   DECIMAL(10,2);
    DECLARE v_offer_status   VARCHAR(20);
    DECLARE v_job_status     VARCHAR(20);
    DECLARE v_new_contract_id INT;

    -- Roll the whole thing back and surface the original error if anything fails.
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Lock and read the offer.
    SELECT job_id, freelancer_id, offer_amount, status
      INTO v_job_id, v_freelancer_id, v_offer_amount, v_offer_status
      FROM Offers
     WHERE offer_id = p_offer_id
     FOR UPDATE;

    IF v_job_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot award: offer not found.';
    END IF;

    IF v_offer_status <> 'Pending' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot award: this offer is not Pending.';
    END IF;

    -- Lock and read the job.
    SELECT status INTO v_job_status
      FROM Jobs
     WHERE job_id = v_job_id
     FOR UPDATE;

    IF v_job_status <> 'Open' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot award: the job is not Open.';
    END IF;

    -- Mark the winning offer Selected and reject the other Pending offers.
    UPDATE Offers SET status = 'Selected'
     WHERE offer_id = p_offer_id;

    UPDATE Offers SET status = 'Rejected'
     WHERE job_id = v_job_id
       AND offer_id <> p_offer_id
       AND status = 'Pending';

    -- Create the contract (new id = MAX + 1).
    SELECT COALESCE(MAX(contract_id), 0) + 1 INTO v_new_contract_id FROM Contracts;
    INSERT INTO Contracts (contract_id, job_id, freelancer_id, final_amount, status)
    VALUES (v_new_contract_id, v_job_id, v_freelancer_id, v_offer_amount, 'In Progress');

    -- Mark the job Awarded (the existing offer/contract triggers also do this;
    -- setting it explicitly keeps the procedure correct on its own).
    UPDATE Jobs SET status = 'Awarded' WHERE job_id = v_job_id;

    COMMIT;
END $$

-- ------------------------------------------------------------
-- 3. New triggers.
-- ------------------------------------------------------------

-- Reject an offer that exceeds the job budget or targets a non-Open job.
DROP TRIGGER IF EXISTS before_insert_offers $$
CREATE TRIGGER before_insert_offers
BEFORE INSERT ON Offers
FOR EACH ROW
BEGIN
    DECLARE v_budget DECIMAL(10,2);
    DECLARE v_status VARCHAR(20);

    SELECT budget_max, status INTO v_budget, v_status
      FROM Jobs WHERE job_id = NEW.job_id;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot add offer: that job does not exist.';
    END IF;

    IF v_status <> 'Open' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot add offer: the job is not Open.';
    END IF;

    IF NEW.offer_amount > v_budget THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot add offer: offer amount exceeds the job budget.';
    END IF;
END $$

-- Completing/cancelling a contract flows back to the job.
DROP TRIGGER IF EXISTS after_update_contracts $$
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
